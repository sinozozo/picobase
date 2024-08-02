package com.picobase.logic.mapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.cache.LazyCache;
import com.picobase.exception.BadRequestException;
import com.picobase.exception.PbException;
import com.picobase.model.*;
import com.picobase.model.schema.MultiValuer;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.JsonOptions;
import com.picobase.model.schema.fieldoptions.RelationOptions;
import com.picobase.persistence.dbx.Query;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.mapper.AbstractMapper;
import com.picobase.persistence.mapper.MappingOptions;
import com.picobase.persistence.model.Index;
import com.picobase.persistence.repository.StorageContextHolder;
import com.picobase.util.PbConstants;
import com.picobase.util.Tokenizer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.picobase.persistence.dbx.DbxUtil.quoteSimpleColumnName;
import static com.picobase.persistence.dbx.expression.Expression.*;
import static com.picobase.persistence.resolver.ListUtil.existInArray;
import static com.picobase.util.PbConstants.CollectionType.View;
import static com.picobase.util.PbConstants.FieldName.*;
import static com.picobase.util.PbConstants.FieldType.Number;
import static com.picobase.util.PbConstants.FieldType.*;
import static com.picobase.util.PbConstants.baseModelFieldNames;


public class CollectionMapper extends AbstractMapper<CollectionModel> {

    public static final String[] ToJsonStrFieldNames = new String[]{"schema", "indexes", "options"};
    private static final long EXPIRED_ACCESS_DURATION = 60 * 60 * 1000; //  60分钟 ,数据过期时间
    private static final long EXPIRE_CHECK_INTERVAL_MILLIS = 5 * 60 * 1000;// 5分钟 , 数据过期检测执行时间
    private static final int MAX_CACHE_SIZE = 10_0000;

    /**
     * Collection 缓存  nameOrId --> CollectionModel
     */
    private LazyCache<String, CollectionModel> cache = new LazyCache<>(MAX_CACHE_SIZE, EXPIRED_ACCESS_DURATION, EXPIRE_CHECK_INTERVAL_MILLIS, null);


    @Override
    public String getTableName() {
        return PbConstants.TableName.COLLECTION;
    }


    public SelectQuery modelQuery() {
        return PbUtil.getPbDbxBuilder().select("*").from(getTableName());
    }


    public boolean isCollectionNameUnique(String name, String... ids) {
        SelectQuery sq = super.modelQuery().select("count(*)").andWhere(newExpr("LOWER(`name`)=:name", Map.of("name", name.toLowerCase()))).limit(1);
        ids = ArrayUtil.removeNull(ids);
        ids = ArrayUtil.distinct(ids);

        if (ids.length > 0) {
            sq.andWhere(notIn("id", (Object[]) ids));
        }

        return sq.count() == 0;
    }

    public CollectionModel findCollectionByNameOrId(String nameOrId) {
        if (StrUtil.isEmpty(nameOrId)) {
            return null;
        }

        return cache.get(nameOrId, () -> modelQuery()
                .andWhere(newExpr("`id` = :id OR LOWER(`name`)=:name"
                        , Map.of("id", nameOrId, "name", nameOrId.toLowerCase())))
                .limit(1).one(CollectionModel.class));

    }


    // CreateViewSchema creates a new view schema from the provided select query.
    //
    // There are some caveats:
    // - The select query must have an "id" column.
    // - Wildcard ("*") columns are not supported to avoid accidentally leaking sensitive data.
    public Schema createViewSchema(String selectQuery) {
        Schema result = Schema.newSchema();

        var suggestedFields = parseQueryToFields(selectQuery);

        String tempView = "_temp_" + RandomUtil.randomString(5);
        try {
            saveView(tempView, selectQuery);

            // extract the generated view table info
            var info = tableInfo(tempView);

            boolean hasId = false;
            for (TableInfoRow row : info) {
                if (Id.equals(row.getName())) {
                    hasId = true;
                }

                if (ArrayUtil.contains(baseModelFieldNames, row.getName())) {
                    continue; // skip base model fields since they are not part of the schema
                }

                SchemaField field;
                if (suggestedFields.containsKey(row.getName())) {
                    field = suggestedFields.get(row.getName()).getField();
                } else {
                    field = new SchemaField(row.getName(), PbConstants.FieldType.Json, new JsonOptions(1));
                }

                result.addField(field);
            }

            if (!hasId) {
                throw new BadRequestException("Missing required id column (you can use `(ROW_NUMBER() OVER()) as id` if you don't have one");
            }
        } catch (Exception e) {
            throw e; // re-throw  tableInfo查询时异常继续上浮
        } finally {
            deleteView(tempView);
        }
        return result;
    }

    // SaveView creates (or updates already existing) persistent SQL view.
    //
    // Be aware that this method is vulnerable to SQL injection and the
    // "selectQuery" argument must come only from trusted input!
    public void saveView(String name, String selectQuery) {
        // delete old view (if exists)
        deleteView(name);

        selectQuery = selectQuery.trim().replaceAll(";$", "");

        // try to eagerly detect multiple inline statements
        try (Tokenizer tk = Tokenizer.newFromString(selectQuery)) {
            tk.setSeparators(';');
            var queryParts = tk.scanAll();
            if (queryParts.size() > 1) {
                throw new BadRequestException("Multiple statements are not supported");
            }
        }
        // (re)create the view
        //
        // note: the query is wrapped in a secondary SELECT as a rudimentary
        // measure to discourage multiple inline sql statements execution.
        createView(name, selectQuery);

        // fetch the view table info to ensure that the view was created
        // because missing tables or columns won't return an error
        try {
            tableInfo(name);
        } catch (Exception e) {
            // manually cleanup previously created view in case the func
            // is called in a nested transaction and the error is discarded
            deleteView(name);
            throw e;
        }

    }

    /**
     * 执行ddl语句
     *
     * @param ddlSql
     * @return
     */
    public boolean exec(String ddlSql) {
        //return PbUtil.getPbDbxBuilder().newQuery(sql).execute() > 0; 这里没有使用dbx执行 ，是因为新建的ModifyRequest 默认是回滚状态的，ddl执行后返回的行数为0，导致抛出异常。
        /**
         * 这里直接使用底层数据库操作对象执行ddl语句
         */
        StorageContextHolder.addSqlContext(ddlSql);
        AtomicBoolean result = new AtomicBoolean(false);
        PbManager.getPbDatabaseOperate().blockUpdate((r, e) -> {
            result.set(e != null);
        });
        return result.get();
    }

    public boolean createTable(String tableName, String cols) {
        return exec("CREATE TABLE " + tableName + " (" + cols + ")");
    }

    public boolean createView(String viewName, String sql) {
        return exec("CREATE VIEW " + viewName + " AS SELECT * FROM (" + sql + ") AS alias_" + viewName);
    }

    public boolean deleteView(String viewName) {
        return exec("DROP VIEW IF EXISTS " + viewName);
    }

    public boolean createIndex(List<String> sqls) {
        return batchExec(sqls);
    }

    private boolean batchExec(List<String> sqls) {
        if (CollUtil.isEmpty(sqls)) {
            return false;
        }

        //批量执行语句
        sqls.forEach(sql -> StorageContextHolder.addSqlContext(sql));
        return PbManager.getPbDatabaseOperate().blockUpdate();
    }

    private boolean renameTable(String oldName, String newName) {
        return exec("ALTER TABLE " + oldName + " RENAME TO " + newName);
    }

    private boolean renameView(String oldName, String newName) {
        return exec("RENAME TABLE " + oldName + " TO " + newName); // rename table book23 to book2;
    }

    private boolean dropColumn(String tableName, String column) {
        return exec("ALTER TABLE " + tableName + " DROP COLUMN " + column);
    }

    private boolean addColumn(String tableName, String column, String type) {
        return exec("ALTER TABLE " + tableName + " ADD " + column + " " + type);
    }

    private boolean renameColumn(String tableName, String oldName, String newName) {
        return exec("ALTER TABLE " + tableName + " RENAME COLUMN " + oldName + " TO " + newName);
    }

    private List<TableInfoRow> tableInfo(String tableName) {
        String query = "DESCRIBE " + tableName;

        try {
            return PbUtil.getPbDbxBuilder().newQuery(query).all((rs, rowNum) -> new TableInfoRow(
                    rs.getInt("Key"),
                    rs.getString("Field"),
                    rs.getString("Type"),
                    rs.getBoolean("Null"),
                    rs.getString("Default")
            ));
        } catch (Exception e) {
            throw new PbException("empty table info probably due to invalid or missing table {}", tableName);
        }


    }

    public Map<String, QueryField> parseQueryToFields(String selectQuery) {
        IdentifiersParser p = Identifier.parse(selectQuery);

        Map<String, CollectionModel> collections = findCollectionsByIdentifiers(p.getTables());
        if (collections == null) {
            return null;
        }

        Map<String, QueryField> result = new HashMap<>();
        Identifier mainTable = new Identifier();

        if (p.getTables().size() > 0) {
            mainTable = p.getTables().get(0);
        }

        for (Identifier col : p.getColumns()) {
            String colLower = col.getOriginal().toLowerCase();

            // numeric aggregations
            if (colLower.startsWith("count(") || colLower.startsWith("total(")) {
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), Number));
                result.put(col.getAlias(), queryField);
                continue;
            }

            Matcher castMatcher = Identifier.castRegex.matcher(colLower);
            // Numeric casts
            if (castMatcher.find() && castMatcher.groupCount() == 1) {
                QueryField queryField = new QueryField();
                String castMatch = castMatcher.group(1);
                switch (castMatch) {
                    case "real", "integer", "int", "decimal", "numeric" -> {
                        queryField.setField(new SchemaField(col.getAlias(), Number));
                        result.put(col.getAlias(), queryField);
                        continue;
                    }
                    case "text" -> {
                        queryField.setField(new SchemaField(col.getAlias(), Text));
                        result.put(col.getAlias(), queryField);
                        continue;
                    }
                    case "boolean", "bool" -> {
                        queryField.setField(new SchemaField(col.getAlias(), Bool));
                        result.put(col.getAlias(), queryField);
                        continue;
                    }
                }
            }

            String[] parts = col.getOriginal().split("\\.");

            String fieldName;
            CollectionModel collection;

            if (parts.length == 2) {
                fieldName = parts[1];
                collection = collections.get(parts[0]);
            } else {
                fieldName = parts[0];
                collection = collections.get(mainTable.getAlias());
            }

            // fallback to the default field if the found column is not from a collection schema
            if (collection == null) {
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), PbConstants.FieldType.Json, new JsonOptions(1)));
                result.put(col.getAlias(), queryField);
                continue;
            }

            if ("*".equals(fieldName)) {
                throw new BadRequestException("Dynamic column names are not supported");
            }

            // find the first field by name (case insensitive)
            SchemaField field = null;
            for (SchemaField f : collection.getSchema().getFields()) {
                if (f.getName().equalsIgnoreCase(fieldName)) {
                    field = f;
                    break;
                }
            }

            if (field != null) {
                SchemaField clone = new SchemaField(field.getName(), field.getType());
                clone.setId(""); // unset to prevent duplications if the same field is aliased multiple times
                clone.setName(col.getAlias());
                QueryField queryField = new QueryField();
                queryField.setField(clone);
                queryField.setCollection(collection);
                queryField.setOriginal(field);
                result.put(col.getAlias(), queryField);
                continue;
            }

            if (fieldName.equals(Id)) {
                // convert to relation since it is a direct id reference
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), Relation, new RelationOptions(1, collection.getId())));
                queryField.setCollection(collection);
                result.put(col.getAlias(), queryField);
            } else if (fieldName.equals(Created) || fieldName.equals(Updated)) {
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), Date));
                queryField.setCollection(collection);
                result.put(col.getAlias(), queryField);
            } else if (fieldName.equals(Username) && collection.isAuth()) {
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), Text));
                queryField.setCollection(collection);
                result.put(col.getAlias(), queryField);
            } else if (fieldName.equals(PbConstants.FieldName.Email) && collection.isAuth()) {
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), PbConstants.FieldType.Email));
                queryField.setCollection(collection);
                result.put(col.getAlias(), queryField);
            } else if ((fieldName.equals(Verified) || fieldName.equals(EmailVisibility)) && collection.isAuth()) {
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), PbConstants.FieldType.Bool));
                queryField.setCollection(collection);
                result.put(col.getAlias(), queryField);
            } else {
                QueryField queryField = new QueryField();
                queryField.setField(new SchemaField(col.getAlias(), PbConstants.FieldType.Json, new JsonOptions(1)));
                queryField.setCollection(collection);
                result.put(col.getAlias(), queryField);
            }
        }

        return result;
    }

    private Map<String, CollectionModel> findCollectionsByIdentifiers(List<Identifier> tables) {
        List<String> names = new ArrayList<>(tables.size());

        for (Identifier table : tables) {
            if (table.getAlias().contains("(")) {
                continue; // Skip expressions
            }
            names.add(table.getOriginal());
        }

        if (names.isEmpty()) {
            return null;
        }

        Map<String, CollectionModel> result = MapUtil.newHashMap(names.size());
        List<CollectionModel> collections = findCollectionsByNames(names);

        for (Identifier table : tables) {
            for (CollectionModel collection : collections) {
                if (collection.getName().equals(table.getOriginal())) {
                    result.put(table.getAlias(), collection);
                }
            }
        }
        return result;
    }

    private List<CollectionModel> findCollectionsByNames(List<String> names) {
        return this.selectList(null, names, null);
    }


    private List<CollectionModel> findCollectionsByExcludeIds(List<String> excludeIds) {
        return this.selectList(null, null, excludeIds);
    }

    private List<CollectionModel> selectList(String type, List<String> names, List<String> excludeIds) {

        SelectQuery selectQuery = modelQuery();
        if (StrUtil.isNotEmpty(type)) {
            selectQuery.andWhere(newExpr("`type` = :type", Map.of("type", type)));
        }

        if (CollUtil.isNotEmpty(names)) {
            selectQuery.andWhere(in("`name`", names));
        }

        if (CollUtil.isNotEmpty(excludeIds)) {
            selectQuery.andWhere(notIn("`id`", excludeIds));
        }


        return selectQuery.build().all(CollectionModel.class);
    }


    // saveViewCollection persists the provided View collection changes:
    //   - deletes the old related SQL view (if any)
    //   - creates a new SQL view with the latest newCollection.Options.Query
    //   - generates a new schema based on newCollection.Options.Query
    //   - updates newCollection.Schema based on the generated view table info and query
    //   - saves the newCollection
    //
    public void saveViewCollection(CollectionModel newCollection, CollectionModel oldCollection) {
        if (!newCollection.isView()) {
            throw new BadRequestException("not a view collection");
        }

        String query = newCollection.viewOptions().getQuery();

        // generate collection schema from the query
        Schema viewSchema = createViewSchema(query);

        // delete old renamed view
        if (oldCollection != null) {
            deleteView(oldCollection.getName());
        }

        // wrap view query if necessary
        query = normalizeViewQueryId(query);

        // (re)create the view
        saveView(newCollection.getName(), query);

        newCollection.setSchema(viewSchema);

        super.insertQuery(newCollection).execute();
    }

    // normalizeViewQueryId wraps (if necessary) the provided view query
    // with a subselect to ensure that the id column is a text since
    // currently we don't support non-string model ids
    // (see https://github.com/pocketbase/pocketbase/issues/3110).
    private String normalizeViewQueryId(String query) {
        query = query.trim().replaceAll(";", "");

        var parsed = parseQueryToFields(query);

        boolean needWrapping = true;

        QueryField idField = parsed.get(Id);
        if (idField != null && idField.getField() != null &&
                !Objects.equals(idField.getField().getType(), Json) &&
                !Objects.equals(idField.getField().getType(), Number) &&
                !Objects.equals(idField.getField().getType(), Bool)) {
            needWrapping = false;
        }

        if (!needWrapping) {
            return query; // no changes needed
        }

        // raw parse to preserve the columns order
        IdentifiersParser rawParsed = Identifier.parse(query);

        List<String> columns = new ArrayList<>();
        for (Identifier col : rawParsed.getColumns()) {
           /* if (col.getAlias().equals(PocketConstant.FieldNameId)) {
                columns.add(String.format("cast(%s as text) %s", col.getAlias(), col.getAlias()));
            } else {*/
            columns.add(col.getAlias());
            /*}*/
        }

        query = String.format("SELECT %s FROM (%s) as inner_query", String.join(",", columns), query);

        return query;
    }


    /**
     * SyncRecordTableSchema compares the two provided collections
     * and applies the necessary related record table changes.
     * <p>
     * If `oldCollection` is null, then only `newCollection` is used to create the record table.
     */
    public void syncRecordTableSchema(CollectionModel newCollection, CollectionModel oldCollection) {
        // create
        // -----------------------------------------------------------
        if (null == oldCollection) {
            Map<String, String> cols = new LinkedHashMap<>();
            cols.put(Id, "VARCHAR(32) PRIMARY KEY NOT NULL");
            cols.put(Created, "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL");
            cols.put(Updated, "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL");

            if (newCollection.isAuth()) {
                cols.put(Username, "VARCHAR(255) NOT NULL");
                cols.put(PbConstants.FieldName.Email, "VARCHAR(255) NOT NULL");
                cols.put(EmailVisibility, "BOOLEAN DEFAULT FALSE NOT NULL");
                cols.put(Verified, "BOOLEAN DEFAULT FALSE NOT NULL");
                cols.put(TokenKey, "VARCHAR(255) NOT NULL");
                cols.put(PasswordHash, "TEXT NOT NULL");
                cols.put(LastResetSentAt, "DATETIME DEFAULT NULL");
                cols.put(LastVerificationSentAt, "DATETIME DEFAULT NULL");
            }

            String tableName = newCollection.getName();


            // add schema field definitions
            newCollection.getSchema().getFields().forEach(field -> cols.put(field.getName(), field.colDefinition()));

            // create table
            String tableSchema = cols.entrySet()
                    .stream()
                    .map(entry -> quoteSimpleColumnName(entry.getKey()) + " " + entry.getValue())
                    .collect(Collectors.joining(","));
            createTable(tableName, tableSchema);

            // add named unique index on the email and tokenKey columns
            if (newCollection.isAuth()) {
                List<String> sqls = CollUtil.newArrayList();
                sqls.add(String.format("CREATE UNIQUE INDEX _%s_username_idx ON %s (`username`);", newCollection.getId(), tableName));
                sqls.add(String.format("CREATE  INDEX _%s_email_idx ON %s (`email`);", newCollection.getId(), tableName));
                sqls.add(String.format("CREATE UNIQUE INDEX _%s_tokenKey_idx ON %s (`tokenKey`);", newCollection.getId(), tableName));
                createIndex(sqls);
            }

            createCollectionIndexes(newCollection);
            return;
        }


        // update
        // -----------------------------------------------------------
        var oldTableName = oldCollection.getName();
        var newTableName = newCollection.getName();
        var oldSchema = oldCollection.getSchema();
        var newSchema = newCollection.getSchema();

        // drop old indexes (if any)
        dropCollectionIndex(oldCollection);

        // check for renamed table
        if (!StrUtil.equals(oldTableName, newTableName)) {
            renameTable(oldTableName, newTableName);
        }

        // check for deleted columns
        for (SchemaField oldField : oldSchema.getFields()) {
            if (null != newSchema.getFieldById(oldField.getId())) {
                continue; // exist
            }

            dropColumn(newTableName, oldField.getName());
        }

        // check for new or renamed columns
        Map<String, String> toRename = MapUtil.newHashMap();
        for (SchemaField field : newSchema.getFields()) {
            var oldField = oldSchema.getFieldById(field.getId());
            // Note:
            // We are using a temporary column name when adding or renaming columns
            // to ensure that there are no name collisions in case there is
            // names switch/reuse of existing columns (eg. name, title -> title, name).
            // This way we are always doing 1 more rename operation but it provides better dev experience.
            if (null == oldField) {
                var tempName = field.getName() + RandomUtil.randomString(5);
                toRename.put(tempName, field.getName());

                // add
                addColumn(newTableName, tempName, field.colDefinition());
            } else if (!StrUtil.equals(oldField.getName(), field.getName())) {
                var tempName = field.getName() + RandomUtil.randomString(5);
                toRename.put(tempName, field.getName());

                // rename
                renameColumn(newTableName, oldField.getName(), tempName);
            }
        }

        // set the actual columns name
        for (Map.Entry<String, String> map : toRename.entrySet()) {
            renameColumn(newTableName, map.getKey(), map.getValue());
        }

        normalizeSingleVsMultipleFieldChanges(newCollection, oldCollection);

        createCollectionIndexes(newCollection);
    }

    private void normalizeSingleVsMultipleFieldChanges(CollectionModel newCollection, CollectionModel oldCollection) {
        if (newCollection.isView() || oldCollection == null) {
            return; // view or not an update
        }

        for (SchemaField newField : newCollection.getSchema().getFields()) {
            // allow to continue even if there is no old field for the cases
            // when a new field is added and there are already inserted data
            boolean isOldMultiple = false;
            SchemaField oldField = oldCollection.getSchema().getFieldById(newField.getId());
            if (oldField != null && oldField.getOptions() instanceof MultiValuer) {
                isOldMultiple = ((MultiValuer) oldField.getOptions()).isMultiple();
            }

            boolean isNewMultiple = false;
            if (newField.getOptions() instanceof MultiValuer) {
                isNewMultiple = ((MultiValuer) newField.getOptions()).isMultiple();
            }

            if (isOldMultiple == isNewMultiple) {
                continue; // no change
            }

            // update the column definition by:
            // 1. inserting a new column with the new definition
            // 2. copy normalized values from the original column to the new one
            // 3. drop the original column
            // 4. rename the new column to the original column
            // -------------------------------------------------------

            String originalName = newField.getName();
            String tempName = "_" + newField.getName() + RandomUtil.randomString(5);
            addColumn(newCollection.getName(), tempName, newField.colDefinition());

            String copyQuery;
            if (!isOldMultiple && isNewMultiple) {
                // single -> multiple (convert to array)
                copyQuery = String.format("""
                                 UPDATE %s set %s = (
                                	CASE
                                		WHEN COALESCE(%s, '') = ''
                                		THEN '[]'
                                		ELSE (
                                			CASE
                                				WHEN JSON_VALID(%s) AND json_type(%s) = 'array'
                                				THEN %s
                                				ELSE JSON_ARRAY(%s)
                                			END
                                		)
                                	END
                                )""", newCollection.getName(),
                        tempName,
                        originalName,
                        originalName,
                        originalName,
                        originalName,
                        originalName);
            } else {
                // multiple -> single (keep only the last element)
                //
                // note: for file fields the actual file objects are not
                // deleted allowing additional custom handling via migration
                copyQuery = String.format("""
                                 UPDATE %s set %s = (
                                	CASE
                                		WHEN COALESCE(%s, '[]') = '[]'
                                		THEN ''
                                		ELSE (
                                			CASE
                                				WHEN JSON_VALID(%s) AND json_type(%s) = 'array'
                                				THEN COALESCE(json_extract(%s, '$[#-1]'), '')
                                				ELSE %s
                                			END
                                		)
                                	END
                                )""", newCollection.getName(),
                        tempName,
                        originalName,
                        originalName,
                        originalName,
                        originalName,
                        originalName);
            }

            // copy the normalized values
            exec(copyQuery);

            // drop the original column
            dropColumn(newCollection.getName(), originalName);

            // rename the new column back to the original
            renameColumn(newCollection.getName(), tempName, originalName);
        }
    }


    private void createCollectionIndexes(CollectionModel collection) {
        if (collection.isView()) {
            return; // views don't have indexes
        }

        // drop new indexes in case a duplicated index name is used
        dropCollectionIndex(collection);
        // upsert new indexes
        //
        // note: we are returning validation errors because the indexes cannot be
        //       validator in a form, aka. before persisting the related collection
        //       record table changes
        if (collection.getIndexes() == null) {
            return;
        }
        for (String idx : collection.getIndexes()) {
            Index parsed = Index.parseIndex(idx);

            if (!parsed.isValid()) {
                throw new BadRequestException("Invalid CREATE INDEX expression.");
            }

            if (!indexExists(collection.getName(), parsed.getIndexName())) {
                createIndex(Collections.singletonList(parsed.build()));
            }
        }
    }


    private void dropCollectionIndex(CollectionModel collection) {
        if (collection.isView()) {
            return; // views don't have indexes
        }

        if (collection.getIndexes() == null) {
            return;
        }

        for (String raw : collection.getIndexes()) {
            Index parsed = Index.parseIndex(raw);
            if (indexExists(collection.getName(), parsed.getIndexName())) {
                dropIndex(collection.getName(), parsed.getIndexName());
            }
        }
    }

    private boolean indexExists(String tableName, String indexName) {
        List<Map> result = PbUtil.getPbDbxBuilder()
                .newQuery("SHOW INDEX FROM " + tableName + " WHERE Key_name = :indexName")
                .bind(Map.of("indexName", indexName)).all(Map.class);
        return CollUtil.isNotEmpty(result);
    }

    private boolean dropIndex(String tableName, String indexName) {
        return exec("ALTER TABLE " + tableName + " DROP INDEX " + indexName);
    }

    private boolean deleteTable(String tableName) {
        return exec("DROP TABLE IF EXISTS " + tableName);
    }

    /**
     * // DeleteCollection deletes the provided Collection model.
     * // This method automatically deletes the related collection records table.
     * //
     * // NB! The collection cannot be deleted, if:
     * // - is system collection (aka. collection.System is true)
     * // - is referenced as part of a relation field in another collection
     */
    public void deleteCollection(CollectionModel collection) {
        if (collection.isSystem()) {
            throw new PbException("system collection {} cannot be deleted", collection.getName());
        }

        // ensure that there aren't any existing references.
        // note: the select is outside of the transaction to prevent SQLITE_LOCKED error when mixing read&write in a single transaction
        Map<CollectionModel, List<SchemaField>> result = findCollectionReferences(collection, collection.getId());
        if (CollUtil.isNotEmpty(result)) {
            List<String> names = new ArrayList<>();
            for (CollectionModel refCollection : result.keySet()) {
                names.add(refCollection.getName());
            }
            throw new BadRequestException(String.format("the collection %s has external relation field references (%s).", collection.getName(), String.join(", ", names)));
        }

        //rename 实体表名
        String tempName = collection.getName() + "_temp_" + RandomUtil.randomString(5);

        if (collection.isView()) {
            renameView(collection.getName(), tempName);
        } else {
            renameTable(collection.getName(), tempName);
        }


        // trigger views resave to check for dependencies
        try {
            resaveViewsWithChangedSchema(collection.getId());
        } catch (Exception e) {
            renameTable(tempName, collection.getName());
            throw new PbException("the collection has a view dependency - {}", e.getMessage());
        }


        // Delete the related view or records table
        if (collection.isView()) {
            deleteView(tempName);
        } else {
            deleteTable(tempName);
        }


        PbUtil.deleteById(collection.getId(), CollectionModel.class);
    }

    /**
     * resaveViewsWithChangedSchema updates all view collections with changed schemas.
     */
    public void resaveViewsWithChangedSchema(String... excludeIds) {
        List<CollectionModel> collections = findCollectionsByType(View);

        for (CollectionModel collection : collections) {
            if (excludeIds.length > 0 && existInArray(collection.getId(), excludeIds)) {
                continue;
            }

            // clone the existing schema so that it is safe for temp modifications
            Schema oldSchema = collection.getSchema().clone();


            // generate a new schema from the query
            Schema newSchema = createViewSchema(collection.viewOptions().getQuery());

            // unset the schema field ids to exclude from the comparison
            oldSchema.getFields().forEach(f -> f.setId(""));
            newSchema.getFields().forEach(f -> f.setId(""));


            if (oldSchema.equals(newSchema)) {
                continue; // no changes
            }

            saveViewCollection(collection, null);
        }
    }

    /**
     * // FindCollectionReferences returns information for all
     * // relation schema fields referencing the provided collection.
     * //
     * // If the provided collection has reference to itself then it will be
     * // also included in the result. To exclude it, pass the collection id
     * // as the excludeId argument.
     */
    public Map<CollectionModel, List<SchemaField>> findCollectionReferences(CollectionModel collection, String... excludeIds) {
        List<CollectionModel> collections = findCollectionsByExcludeIds(Arrays.asList(excludeIds));
        Map<CollectionModel, List<SchemaField>> result = MapUtil.newHashMap();
        for (CollectionModel c : collections) {
            for (SchemaField f : c.getSchema().getFields()) {
                if (!StrUtil.equals(f.getType(), Relation)) {
                    continue;
                }
                f.initOptions();
                RelationOptions options = (RelationOptions) f.getOptions();
                if (options != null && Objects.equals(options.getCollectionId(), collection.getId())) {
                    List<SchemaField> fields = result.getOrDefault(c, new ArrayList<>());
                    fields.add(f);
                    result.put(c, fields);
                }
            }
        }

        return result;
    }

    /**
     * 重写 insert 方法， 将schema序列化成json
     *
     * @param data Collection
     * @return
     */


    /**
     * 实现Collection的保(存重写根方法)
     *
     * @param data
     * @param options
     * @return
     */
    @Override
    public Query insertQuery(Object data, MappingOptions options) {
        if (data instanceof CollectionModel) {

            options.setFieldValueEditor(((fieldName, fieldValue) -> {
                if (ArrayUtil.contains(ToJsonStrFieldNames, fieldName)) {
                    return PbManager.getPbJsonTemplate().toJsonString(fieldValue);
                }
                return fieldValue;
            }));

            return super.insertQuery(data, options);
        }
        throw new RuntimeException("数据类型不匹配");
    }

    @Override
    public Query updateQuery(Object data, Expression where, MappingOptions options) {
        if (data instanceof CollectionModel) {
            options.setFieldValueEditor(((fieldName, fieldValue) -> {
                if (ArrayUtil.contains(ToJsonStrFieldNames, fieldName)) {
                    return PbManager.getPbJsonTemplate().toJsonString(fieldValue);
                }
                return fieldValue;
            }));

            return super.updateQuery(data, where, options);
        }
        throw new RuntimeException("数据类型不匹配");
    }

    public List<CollectionModel> findCollectionsByType(String collectionType) {
        return modelQuery().andWhere(Expression.newHashExpr(Map.of("type", collectionType))).orderBy("created ASC").all(CollectionModel.class);
    }

    public void removeCache(String nameOrId) {
        cache.remove(nameOrId);
    }
}
