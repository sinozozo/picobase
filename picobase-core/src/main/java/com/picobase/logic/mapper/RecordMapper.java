package com.picobase.logic.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.exception.BadRequestException;
import com.picobase.exception.PbException;
import com.picobase.model.*;
import com.picobase.model.schema.MultiValuer;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.RelationOptions;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.mapper.AbstractMapper;
import com.picobase.persistence.resolver.ListUtil;
import com.picobase.persistence.resolver.ResultCouple;
import com.picobase.util.PbConstants;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.picobase.model.RecordModel.newRecordsFromNullStringMaps;
import static com.picobase.persistence.dbx.DbxUtil.*;
import static com.picobase.persistence.dbx.expression.Expression.*;
import static com.picobase.persistence.resolver.DbUtil.hasSingleColumnUniqueIndex;
import static com.picobase.persistence.resolver.DbUtil.jsonEach;
import static com.picobase.persistence.resolver.ListUtil.toUniqueStringList;
import static com.picobase.util.PbConstants.FieldType.Relation;
import static com.picobase.util.PbConstants.IndirectExpandRegexPattern;

public class RecordMapper extends AbstractMapper<RecordModel> {
    /**
     * MaxExpandDepth specifies the max allowed nested expand depth path.
     */
    public static final int MaxExpandDepth = 6;
    CollectionMapper collectionMapper = new CollectionMapper();

    public Optional<RecordModel> findRecordById(String collectionNameOrId, String recordId, Consumer<SelectQuery>... optFilters) {
        CollectionModel collection = collectionMapper.findCollectionByNameOrId(collectionNameOrId);
        if (collection == null) {
            throw new RuntimeException(String.format("Collection %s not found", collectionNameOrId));
        }
        SelectQuery recordQuery = this.recordQuery(collection);
        SelectQuery query = recordQuery.andWhere(newHashExpr(Map.of(collection.getName() + ".id", recordId)));
        Arrays.stream(optFilters).filter(Objects::nonNull).forEach(filter -> filter.accept(recordQuery));
        return Optional.of(query.limit(1).one(new RecordRowMapper(collection)));
    }

    /**
     * ExpandRecords expands the relations of the provided Record models list.
     * <p>
     * If optFetchFunc is not set, then a default function will be used
     * that returns all relation records.
     * <p>
     * Returns a map with the failed expand parameters and their errors.
     */
    public Map<String, Error> expandRecords(List<RecordModel> records, List<String> expands, ExpandFetchFunc optFetchFunc) {
        List<String> normalized = normalizeExpands(expands);

        Map<String, Error> failed = new HashMap<>();

        normalized.forEach(expand -> {
            var err = this.expand(records, expand, optFetchFunc, 1);
            if (err != null) {
                failed.put(expand, err);
            }
        });
        return failed;
    }

    /**
     * // notes:
     * // - if fetchFunc is nil, dao.FindRecordsByIds will be used
     * // - all records are expected to be from the same collection
     * // - if MaxExpandDepth is reached, the function returns nil ignoring the remaining expand path
     * <p>
     * 1. 初始化 fetchFunc
     * 2. 判断递归请求是否继续
     * 3. 根据records 中的第一个element 获取 Collection
     * 4. 切割 expandPath ， 并获取第一个part 进行正则匹配，看是否有 _via_关键字（back relation逻辑）
     * 5.如果是back relation逻辑
     * <p>
     * <p>
     * 6.如果是 direct relation
     * 6.1 根据 path 获取collection中的field（relationField），校验，初始化为RelationOptions
     * 6.2 根据 初始化为RelationOptions 获取对应的关联的Collection ，校验是否为空。
     * <p>
     * 7. 迭代原始records 并获对应relation字段中的值，该值可能是个集合也可能是单个值。将这些信息收集到变量 relIds 中。
     * 8. 根据上面获取到的关联collection以及这些relIds 执行fetchFunc 获取所有关联的 record
     * 9. 如果expand paths 长度大于1，说明还有更expand内容需要继续递归
     * 10. 如果 paths <1,则是最后一个expand内容 ，这时将所有的 关联的records 即relrecods 进行 id->record 进行索引map构建。
     * 11. 循环原始 recods，开始单个处理
     * <p>
     * 11.1 取出 关系字段中的值（单值或list）
     * 11.2 循环到 步骤10 中的 索引map 中查找，最终形成 有效的 validRels集合。
     * 11.3 如果 validRels是空的则继续循环下一个原始 record
     * 11.4 不为空 从原始record中获取 expand，并判断是否为null， 为null则初始化。
     * 11.5 尝试从这个获取到的expand中 根据 关联字段 relField 的名字获取 expand中的数据。
     * 11.6 分三种情况 将老的expand数据 放到 临时变量 oldExpandedRels中。
     * 11.7 开始merge 数据，  merge过程是 开启嵌套循环 ，先取出老的oldExpandedRel ，在有效的 validRels中根据id匹配，匹配到则调用 mergeExpand方法。
     * 11.8 更新原始 records expand数据。
     */
    private Error expand(List<RecordModel> records, String expandPath, ExpandFetchFunc fetchFunc, int recursionLevel) {
        if (fetchFunc == null) {
            // load a default fetchFunc
            fetchFunc = (relCollection, relIds) -> {
                try {
                    List<RecordModel> rds = this.findRecordByIds(relCollection.getId(), relIds);
                    return new ResultCouple<>(rds);
                } catch (Exception e) {
                    return new ResultCouple<>(null, new Error(e));
                }
            };
        }

        if (StrUtil.isEmpty(expandPath) || recursionLevel > MaxExpandDepth || records.isEmpty()) {
            return null;
        }

        var mainCollection = records.get(0).getCollection();

        SchemaField relField;
        RelationOptions relFieldOptions;
        CollectionModel relCollection;

        var parts = expandPath.split("\\.", 2);

        Matcher matcher = IndirectExpandRegexPattern.matcher(parts[0]);
        if (matcher.matches() && matcher.groupCount() == 2) {
            // back relation
            Optional<CollectionModel> indirectRelOpt = this.findCollectionByNameOrId(matcher.group(1));

            if (indirectRelOpt.isEmpty()) {
                return new Error(String.format("Couldn't find indirect collection %s.", matcher.group(1)));
            }

            var indirectRel = indirectRelOpt.get();
            var indirectRelField = indirectRel.getSchema().getFieldByName(matcher.group(2));
            if (indirectRelField == null || !indirectRelField.getType().equals(Relation)) {
                return new Error(String.format("couldn't find back-relation field %s in collection %s", matcher.group(2), indirectRel.getName()));
            }

            indirectRelField.initOptions();
            var indirectRelFieldOptions = (RelationOptions) indirectRelField.getOptions();
            if (indirectRelFieldOptions == null || !indirectRelFieldOptions.getCollectionId().equals(mainCollection.getId())) {
                return new Error(String.format("invalid back-relation field path %s", matcher.group(0)));
            }

            // add the related id(s) as a dynamic relation field value to
            // allow further expand checks at later stage in a more unified manner
            Supplier<Error> prepErr = () -> {
                try {
                    var q = PbUtil.getPbDbxBuilder().select(String.format("`%s`.`id`", indirectRel.getName()))
                            .from(indirectRel.getName())
                            .limit(1000);// the limit is arbitrary chosen and may change in the future

                    if (indirectRelFieldOptions.isMultiple()) {
                        q.innerJoin(
                                String.format("( SELECT `id` FROM %s,%s `je` WHERE `je`.`value` = :id ) je2", indirectRel.getName(), jsonEach(indirectRelField.getName()))
                                , newExpr(String.format("`je2`.`id` = `%s`.`id`", indirectRel.getName())));

                        /*q.andWhere(exists(newExpr(
                                String.format("SELECT 1 FROM %s je WHERE je.`value` = :id",
                                        jsonEach(indirectRelField.getName()))
                        )));*/
                    } else {
                        q.andWhere(newExpr(quoteColumnName(indirectRelField.getName()) + " = :id"));
                    }


                    records.forEach(r -> {
                        List<String> relIds;
                        try {
                            relIds = q.build().bind(Map.of("id", r.getId())).column(String.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        if (!relIds.isEmpty()) {
                            r.set(parts[0], relIds);
                        }
                    });

                    return null;
                } catch (Exception e) {
                    return new Error(e);
                }
            };

            Error error = prepErr.get();
            if (error != null) {
                return error;
            }
            relFieldOptions = new RelationOptions(null, indirectRel.getId());
            if (hasSingleColumnUniqueIndex(indirectRelField.getName(), indirectRel.getIndexes())) {
                relFieldOptions.setMaxSelect(1);
            }
            // indirect/back relation

            relField = new SchemaField()
                    .setId("_" + parts[0] + RandomUtil.randomString(3))
                    .setType(Relation)
                    .setName(parts[0])
                    .setOptions(relFieldOptions);
            relCollection = indirectRel;

        } else {
            // direct relation
            relField = mainCollection.getSchema().getFieldByName(parts[0]);
            if (relField == null || !relField.getType().equals(Relation)) {
                return new Error(String.format("Couldn't find relation field %s in collection %s.", parts[0], mainCollection.getName()));
            }
            relField.initOptions();
            relFieldOptions = (RelationOptions) relField.getOptions();
            if (relFieldOptions == null) {
                return new Error(String.format("Couldn't initialize the options of relation field %s.", parts[0]));
            }

            Optional<CollectionModel> coOpt = this.findCollectionByNameOrId(relFieldOptions.getCollectionId());
            if (coOpt.isEmpty()) {
                return new Error(String.format("Couldn't find related collection %s.", relFieldOptions.getCollectionId()));
            }
            relCollection = coOpt.get();
        }
        //--------------------------------------------------------

        // extract the id of the relations to expand
        List<String> relIds = new ArrayList<>(records.size());
        records.forEach(r -> relIds.addAll(r.getStringList(relField.getName())));

        // fetch the related records
        ResultCouple<List<RecordModel>> apply = fetchFunc.apply(relCollection, relIds);
        if (apply.getError() != null) {
            return apply.getError();
        }
        var rels = apply.getResult();

        // expand the nested fields
        if (parts.length > 1) {
            var err = this.expand(rels, parts[1], fetchFunc, recursionLevel + 1);
            if (err != null) {
                return err;
            }
        }

        // 使用 Stream API 将 rels 重新索引为 map
        Map<String, RecordModel> indexedRels = rels.stream()
                .collect(Collectors.toMap(RecordModel::getId, rel -> rel));


        records.forEach(model -> {
            var relIds2 = model.getStringList(relField.getName());

            List<RecordModel> validRels = relIds2.stream()
                    .map(indexedRels::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (validRels.isEmpty()) {
                return; // no valid relations
            }

            var expandData = model.expand();


            // normalize access to the previously expanded rel records (if any)
            List<RecordModel> oldExpandedRels = new ArrayList<>();

            Object v = expandData.get(relField.getName());
            if (v == null) {
                // no old expands
            } else if (v instanceof RecordModel v2) {
                oldExpandedRels.add(v2);
            } else if (v instanceof List list) {
                oldExpandedRels = list;
            }

            // merge expands
            for (RecordModel oldExpandedRel : oldExpandedRels) {
                // find a matching rel record
                for (RecordModel rel : validRels) {
                    if (!rel.getId().equals(oldExpandedRel.getId())) {
                        continue;
                    }
                    rel.mergeExpand(oldExpandedRel.expand());
                }
            }

            // update the expanded data
            if (relFieldOptions.getMaxSelect() != null && relFieldOptions.getMaxSelect() <= 1) {
                expandData.put(relField.getName(), validRels.get(0));
            } else {
                expandData.put(relField.getName(), validRels);
            }

            model.setExpand(expandData);

        });


        return null;
    }

    /**
     * normalizeExpands normalizes expand strings and merges self containing paths
     * (eg. ["a.b.c", "a.b", "   test  ", "  ", "test"] -> ["a.b.c", "test"]).
     */
    private List<String> normalizeExpands(List<String> paths) {
        // normalize paths
        List<String> normalized = paths.stream()
                .map(p -> p.replace(" ", "")) // replace spaces
                .map(p -> p.replaceAll("^\\.*|\\.*$", "")) // trim incomplete paths
                .filter(p -> !p.isEmpty())
                .toList();

        // merge containing paths
        List<String> result = IntStream.range(0, normalized.size())
                .filter(i -> {
                    boolean skip = false;
                    for (int j = 0; j < normalized.size(); j++) {
                        if (i == j) continue;
                        if (normalized.get(j).startsWith(normalized.get(i) + ".")) {
                            // skip because there is more detailed expand path
                            skip = true;
                            break;
                        }
                    }
                    return !skip;
                })
                .mapToObj(normalized::get)
                .collect(Collectors.toList());

        return ListUtil.toUniqueStringList(result);
    }

    public SelectQuery recordQuery(CollectionModel collection) {
        var tableName = collection.getName();
        var selectCols = String.format("%s.*", quoteSimpleColumnName(tableName));
        return PbUtil.getPbDbxBuilder().select(selectCols).from(tableName);
    }

    /**
     * FindRecordsByIds finds all Record models by the provided ids.
     * If no records are found, returns an empty List.
     */
    public List<RecordModel> findRecordByIds(String collectionNameOrId, List<String> relIds, Consumer<SelectQuery>... optFilters) {
        Optional<CollectionModel> collection = findCollectionByNameOrId(collectionNameOrId);
        if (collection == null) {
            throw new IllegalStateException("collection is null");
        }
        var query = this.recordQuery(collection.get())
                .andWhere(in(collection.get().getName() + ".id", toUniqueStringList(relIds)));

        if (optFilters != null) {
            Arrays.stream(optFilters).filter(Objects::nonNull).forEach(filter -> filter.accept(query));
        }

        List<RecordModel> all = query.all(new RecordRowMapper(collection.get()));
        if (all != null) {
            return all;
        }
        return Collections.emptyList();
    }

    /**
     * FindCollectionByNameOrId finds a single collection by its name (case insensitive) or id.
     */
    public Optional<CollectionModel> findCollectionByNameOrId(String nameOrId) {
        return Optional.of(collectionMapper.findCollectionByNameOrId(nameOrId));
    }

    /**
     * IsRecordValueUnique checks if the provided key-value pair is a unique Record value.
     * <p>
     * For correctness, if the collection is "auth" and the key is "username",
     * the unique check will be case insensitive.
     * <p>
     * NB! Array values (eg. from multiple select fields) are matched
     * as a serialized json strings (eg. `["a","b"]`), so the value uniqueness
     * depends on the elements order. Or in other words the following values
     * are considered different: `[]string{"a","b"}` and `[]string{"b","a"}`
     */
    public boolean isRecordValueUnique(String collectionNameOrId, String key, Object value, String... excludeIds) {
        Optional<CollectionModel> collectionOpt = findCollectionByNameOrId(collectionNameOrId);
        if (!collectionOpt.isPresent()) {
            return false;
        }

        CollectionModel collection = collectionOpt.get();

        Expression expr;
        if (collection.isAuth() && PbConstants.FieldName.Username.equals(key)) {
            expr = Expression.newExpr("LOWER(`" + PbConstants.FieldName.Username + "`)=:username", Map.of("username", String.valueOf(value).toLowerCase()));
        } else {
            Object normalizedVal;
            if (value instanceof String[]) {
                normalizedVal = List.of(value);
            } else if (value != null && value.getClass().isArray()) {
                normalizedVal = List.of(value);
            } else {
                normalizedVal = value;
            }

            expr = Expression.newHashExpr(Map.of(columnify(key), normalizedVal));
        }


        SelectQuery query = recordQuery(collection).
                select("count(*) as total").
                andWhere(expr).
                limit(1);

        Set<String> uniqueExcludeIds = Arrays.stream(excludeIds).filter(id -> !StrUtil.isBlank(id)).collect(Collectors.toSet());
        if (!uniqueExcludeIds.isEmpty()) {
            query.andWhere(Expression.notIn(collection.getName() + ".id", uniqueExcludeIds));
        }

        Map<String, Object> row = query.row();

        return Integer.parseInt(row.get("total").toString()) == 0;
    }

    public String suggestUniqueAuthRecordUsername(String collectionNameOrId, String baseUsername, String... excludeIds) {
        String username = baseUsername;
        for (int i = 0; i < 10; i++) {
            boolean isUnique = isRecordValueUnique(collectionNameOrId, PbConstants.FieldName.Username, username, excludeIds);
            if (isUnique) {
                break;
            }
            username = baseUsername + RandomUtil.randomInt(3 + i);
        }
        return username;
    }

    public void createRecord(RecordModel model) {
        if (!model.hasId()) {
            //自动生成 id
            model.refreshId();
        }


        if (model.getCreated() == null) {
            model.refreshCreated();
        }

        if (model.getUpdated() == null) {
            model.refreshUpdated();
        }

        // record 数据保存
        var dataMap = model.columnValueMap();
        if (StrUtil.isEmpty(String.valueOf(dataMap.get("id")))) {
            dataMap.put("id", model.getId());
        }
        super.insertQuery(dataMap).execute();


    }

    public void updateRecord(RecordModel model) {
        if (!model.hasId()) {
            throw new BadRequestException("missing model id");
        }

        if (model.getCreated() == null) {
            model.refreshCreated();
        }

        model.refreshUpdated();

        /**
         * 执行更新操作，这里没有调用父类 update ， 因为model是动态的 record ，并不知道具体操作哪张表
         */
        PbUtil.getPbDbxBuilder().update(model.tableName(), BeanUtil.beanToMap(model.columnValueMap()), newHashExpr(Map.of("id", model.getId()))).execute();

    }

    /**
     * // DeleteRecord deletes the provided Record model.
     * //
     * // This method will also cascade the delete operation to all linked
     * // relational records (delete or unset, depending on the rel settings).
     * //
     * // The delete operation may fail if the record is part of a required
     * // reference in another record (aka. cannot be deleted or unset).
     */

    public void deleteRecord(RecordModel record) {


        // fetch rel references (if any)
        //
        // note: the select is outside of the transaction to minimize
        // SQLITE_BUSY errors when mixing read&write in a single transaction
        Map<CollectionModel, List<SchemaField>> refs = collectionMapper.findCollectionReferences(record.getCollection());
        PbManager.getPbDatabaseOperate().runInTransaction(state -> {
            ExternalAuthMapper externalAuthMapper = PbUtil.findMapper(ExternalAuthModel.class);

            // manually trigger delete on any linked external auth to ensure
            // that the `OnModel*` hooks are triggered
            if (record.getCollection().isAuth()) {
                List<ExternalAuthModel> externalAuths = externalAuthMapper.findAllExternalAuthsByRecord(record);
                externalAuths.forEach(ea -> {
                    PbUtil.deleteById(ea.getId(), ExternalAuthModel.class);
                });

            }
            // delete the record before the relation references to ensure that there
            // will be no "A<->B" relations to prevent deadlock when calling DeleteRecord recursively
            PbUtil.deleteById(record.getId(), RecordModel.class);
            cascadeRecordDelete(record, refs);
            return null;
        }, false);

    }

    public void cascadeRecordDelete(RecordModel mainRecord, Map<CollectionModel, List<SchemaField>> refs) {
        // Sort the refs keys to ensure that the cascade events firing order is always the same.
        // This is not necessary for the operation to function correctly but it helps having deterministic output during testing.
        List<CollectionModel> sortedRefKeys = new ArrayList<>(refs.keySet());
        Collections.sort(sortedRefKeys, Comparator.comparing(CollectionModel::getName));
        for (CollectionModel refCollection : sortedRefKeys) {
            List<SchemaField> fields = refs.get(refCollection);

            if (refCollection.isView() || fields == null) {
                continue; // skip missing or view collections
            }

            for (SchemaField field : fields) {
                String recordTableName = columnify(refCollection.getName());
                String prefixedFieldName = recordTableName + "." + columnify(field.getName());

                var query = recordQuery(refCollection);

                if (field.getOptions() instanceof MultiValuer) {
                    MultiValuer opt = (MultiValuer) field.getOptions();
                    if (!opt.isMultiple()) {
                        query.andWhere(Expression.newHashExpr(Map.of(prefixedFieldName, mainRecord.getId())));
                    } else {
                        query.andWhere(Expression.exists(Expression.newExpr(String.format(
                                "SELECT 1 FROM %s, JSON_TABLE(CASE WHEN JSON_VALID(%s) THEN %s ELSE JSON_ARRAY(%s) END, '$[*]' COLUMNS(VALUE VARCHAR(255) PATH '$')) AS __je__ WHERE __je__.value = :jevalue",
                                recordTableName, prefixedFieldName, prefixedFieldName, prefixedFieldName), Map.of("jevalue", mainRecord.getId()))));
                    }
                }

                if (Objects.equals(refCollection.getId(), mainRecord.getCollection().getId())) {
                    query.andWhere(Expression.not(Expression.newHashExpr(Map.of(recordTableName + ".id", mainRecord.getId()))));
                }

                int batchSize = 4000;
                List<RecordModel> rows;
                while (true) {
                    query.limit(batchSize);
                    rows = query.all(new RecordRowMapper(refCollection));

                    int total = rows.size();
                    if (total == 0) {
                        break;
                    }

                    List<Map<String, Object>> originalDatas = rows.stream()
                            .map(RecordModel::getOriginalData).toList();
                    List<RecordModel> refRecords = newRecordsFromNullStringMaps(refCollection, originalDatas);

                    deleteRefRecords(mainRecord, refRecords, field);

                    if (total < batchSize) {
                        break; // no more items
                    }

                    rows.clear(); // keep allocated memory
                }
            }
        }
    }

    public void deleteRefRecords(RecordModel mainRecord, List<RecordModel> refRecords, SchemaField field) {
        RelationOptions options = (RelationOptions) field.getOptions();
        if (options == null) {
            throw new BadRequestException("relation field options are not initialized");
        }

        for (RecordModel refRecord : refRecords) {
            List<String> ids = refRecord.getStringList(field.getName());

            // unset the record id
            for (int i = ids.size() - 1; i >= 0; i--) {
                if (ids.get(i).equals(mainRecord.getId())) {
                    ids.remove(i);
                    break;
                }
            }

            // cascade delete the reference
            // (only if there are no other active references in case of multiple select)
            if (options.isCascadeDelete() && ids.size() == 0) {
                // deleteRecord(refRecord);
                // no further actions are needed (the reference is deleted)
                continue;
            }

            if (field.isRequired() && ids.size() == 0) {
                throw new BadRequestException(String.format("the record cannot be deleted because it is part of a required reference in record %s (%s collection)", refRecord.getId(), refRecord.getCollection().getName()));
            }

            // save the reference changes
            refRecord.set(field.getName(), field.prepareValue(ids));
            new RecordUpsert(refRecord).saveRecord();

        }
    }

    @Override
    public String getTableName() {
        return PbUtil.getCurrentCollection().getName();
    }

    /**
     * // FindRecordByViewFile returns the original models.Record of the
     * // provided view collection file.
     */
    public RecordModel findRecordByViewFile(CollectionModel view, String fileFieldName, String filename) {
        Assert.notNull(view);
        Assert.isTrue(view.isView(), "not a view collection");
        QueryField qf = findFirstNonViewQueryFileField(1, view, fileFieldName);

        String cleanFieldName = columnify(qf.getOriginal().getName());

        SelectQuery query = this.recordQuery(qf.getCollection()).limit(1);

        if (!(qf.getOriginal().getOptions() instanceof MultiValuer options) || !options.isMultiple()) {
            query.andWhere(newHashExpr(Map.of(cleanFieldName, filename)));
        } else {
            //TODO
            throw new RuntimeException("not implemented");
        }

        return query.one(new RecordRowMapper(qf.getCollection()));
    }

    private QueryField findFirstNonViewQueryFileField(int level, CollectionModel view, String fileFieldName) {
        // check the level depth to prevent infinite circular recursion
        // (the limit is arbitrary and may change in the future)
        Assert.isTrue(level <= 5, "reached the max recursion level of view collection file field queries");

        Map<String, QueryField> queryFields = collectionMapper.parseQueryToFields(view.viewOptions().getQuery());

        for (Map.Entry<String, QueryField> stringQueryFieldEntry : queryFields.entrySet()) {
            var v = stringQueryFieldEntry.getValue();
            if (v.getCollection() == null || v.getOriginal() == null || !v.getField().getName().equals(fileFieldName)) {
                continue;
            }
            if (v.getCollection().isView()) {
                return findFirstNonViewQueryFileField(level + 1, v.getCollection(), v.getOriginal().getName());
            }
            return v;
        }

        throw new PbException("no query file field found");
    }


    @FunctionalInterface
    public interface ExpandFetchFunc extends BiFunction<CollectionModel, List<String>, ResultCouple<List<RecordModel>>> {

    }


    public RecordModel findAuthRecordByEmail(CollectionModel collection, String identity) {
        if (!collection.isAuth()) {
            throw new IllegalStateException(collection.getName() + " is not an auth collection");
        }

        SelectQuery recordQuery = this.recordQuery(collection);
        SelectQuery query = recordQuery.andWhere(newHashExpr(Map.of(PbConstants.FieldName.Email, identity)));
        return query.limit(1).one(new RecordRowMapper(collection));
    }

    public RecordModel findAuthRecordByUsername(CollectionModel collection, String identity) {
        if (!collection.isAuth()) {
            throw new IllegalStateException(collection.getName() + " is not an auth collection");
        }

        SelectQuery recordQuery = this.recordQuery(collection);
        SelectQuery query = recordQuery.andWhere(newHashExpr(Map.of(PbConstants.FieldName.Username, identity)));
        return query.limit(1).one(new RecordRowMapper(collection));
    }
}
