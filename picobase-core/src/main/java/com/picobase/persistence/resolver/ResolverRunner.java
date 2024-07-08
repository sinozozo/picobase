package com.picobase.persistence.resolver;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.picobase.PbManager;
import com.picobase.log.PbLog;
import com.picobase.model.CollectionModel;
import com.picobase.model.schema.MultiValuer;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.RelationOptions;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.util.PbConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.picobase.persistence.dbx.DbxUtil.columnify;
import static com.picobase.persistence.dbx.DbxUtil.splitModifier;
import static com.picobase.persistence.dbx.expression.Expression.newExpr;
import static com.picobase.persistence.resolver.DbUtil.hasSingleColumnUniqueIndex;
import static com.picobase.persistence.resolver.ListUtil.existInArray;
import static com.picobase.persistence.resolver.ListUtil.existInListWithRegex;
import static com.picobase.util.PbConstants.*;

public class ResolverRunner {
    private static final PbLog log = PbManager.getLog();

    // maxNestedRels defines the max allowed nested relations depth.
    private static final int maxNestedRels = 6;

    /**
     * indicates whether the runner was already executed
     */
    private boolean used;
    /**
     * resolver is the shared expression fields resolver
     */
    private final RecordFieldResolver resolver;
    /**
     * the name of the single field expression the runner is responsible for
     */
    private final String fieldName;

    // shared processing state
    // ---------------------------------------------------------------

    /**
     * holds the active props that remains to be processed
     */
    private List<String> activeProps;
    /**
     * the last used collection name
     */
    private String activeCollectionName;
    /**
     * the last used table alias
     */
    private String activeTableAlias;
    /**
     * indicates whether hidden fields (eg. email) should be allowed without extra conditions
     */
    private boolean allowHiddenFields;
    /**
     * indicating whether to return null on missing field or return an error
     */
    private boolean nullifyMisingField;
    /**
     * indicates whether to attach a multiMatchSubquery condition to the ResolverResult
     */
    private boolean withMultiMatch;
    /**
     * the last used multi-match table alias
     */
    private String multiMatchActiveTableAlias;
    /**
     * the multi-match subquery expression generated from the fieldName
     */
    private MultiMatchSubquery multiMatch;


    public ResolverRunner(String fieldName, RecordFieldResolver resolver) {
        this.fieldName = fieldName;
        this.resolver = resolver;
    }

    public ResolverResult run() {
        if (this.used) {
            throw new RuntimeException("the runner was already used");
        }

        if (!this.resolver.getAllowedFields().isEmpty() && !existInListWithRegex(this.fieldName, this.resolver.getAllowedFields())) {
            throw new RuntimeException("failed to resolve field :" + this.fieldName);
        }
        try {
            this.prepare();

            // check for @collection field (aka. non-relational join)
            // must be in the format "@collection.COLLECTION_NAME.FIELD[.FIELD2....]"
            if (this.activeProps.get(0).equals("@collection")) {
                return this.processCollectionField();
            }

            if (this.activeProps.get(0).equals("@request")) {
                if (this.resolver.getRequestInfo() == null) {
                    return ResolverResult.builder().identifier("NULL").build();
                }

                if (this.fieldName.startsWith("@request.auth.")) {
                    return this.processRequestAuthField();
                }

                if (this.fieldName.startsWith("@request.data.") && this.activeProps.size() > 2) {
                    var arr = splitModifier(this.activeProps.get(2));
                    var name = arr[0];
                    var modifier = arr[1];

                    var dataField = this.resolver.getBaseCollection().getSchema().getFieldByName(name);
                    if (dataField == null) {
                        return this.resolver.resolveStaticRequestField(this.activeProps.subList(1, this.activeProps.size()));
                    }

                    dataField.initOptions();

                    //check for data relation field
                    if (dataField.getType().equals(PbConstants.FieldType.Relation) && this.activeProps.size() > 3) {
                        return this.processRequestInfoRelationField(dataField);
                    }

                    // check for data arrayble fields ":each" modifier
                    if (EachModifier.equals(modifier) && existInArray(dataField.getType(), ArraybleFieldTypes()) && this.activeProps.size() == 3) {
                        return this.processRequestInfoEachModifier(dataField);
                    }

                    //check for data arrayble fields ":length" modifier
                    if (LengthModifier.equals(modifier) && existInArray(dataField.getType(), ArraybleFieldTypes()) && this.activeProps.size() == 3) {
                        return this.processRequestInfoLengthModifier(dataField);
                    }
                }
                // some other @request.* static field

                return this.resolver.resolveStaticRequestField(this.activeProps.subList(1, this.activeProps.size()));
            }

            // regular field
            return this.processActiveProps();

        } catch (Exception e) {
            log.error("failed to resolve field :" + this.fieldName, e);
            throw new RuntimeException("failed to resolve field :" + this.fieldName, e);
        } finally {
            this.used = true;
        }
    }

    private ResolverResult processRequestInfoLengthModifier(SchemaField dataField) {
        var dataItems = ListUtil.toUniqueStringList(this.resolver.getRequestInfo().getData().get(dataField.getName()));

        var result = ResolverResult.builder().identifier(String.valueOf(dataItems.size()));

        return result.build();
    }

    private ResolverResult processRequestInfoEachModifier(SchemaField dataField) {
        if (!(dataField.getOptions() instanceof MultiValuer options)) {
            throw new RuntimeException("field " + dataField.getName() + " options are not initialized or doesn't support multivaluer operations");
        }

        var dataItems = ListUtil.toUniqueStringList(this.resolver.getRequestInfo().getData().get(dataField.getName()));

        var rawJson = PbManager.getPbJsonTemplate().toJsonString(dataItems);

        var placeholder = "dataEach" + RandomUtil.randomString(4);
        var cleanFieldName = columnify(dataField.getName());
        var jeTable = String.format("JSON_TABLE( :%s, '$[*]' COLUMNS ( `value` VARCHAR(255) PATH '$' ) )", placeholder);
        var jeAlias = "__dataEach_" + cleanFieldName + "_je";
        this.resolver.registerJoin(jeTable, jeAlias, newExpr("1=1"));

        var result = ResolverResult.builder().identifier(String.format("%s.value", jeAlias)).params(Map.of(placeholder, rawJson));

        if (options.isMultiple()) {
            this.withMultiMatch = true;
        }

        if (this.withMultiMatch) {
            var placeholder2 = "mm" + placeholder;
            var jeTable2 = String.format("JSON_TABLE( :%s, '$[*]' COLUMNS ( `value` VARCHAR(255) PATH '$' ) )", placeholder2);
            var jeAlias2 = "__mm" + jeAlias;

            this.multiMatch.getJoins().add(
                    new Join(jeTable2, jeAlias2, newExpr("1=1"))
            );
            this.multiMatch.getParams().put(placeholder2, rawJson);
            this.multiMatch.setValueIdentifier(String.format("%s.value", jeAlias2));

            result.multiMatchSubQuery(this.multiMatch);
        }

        return result.build();
    }

    private ResolverResult processRequestInfoRelationField(SchemaField dataField) {
        if (!(dataField.getOptions() instanceof RelationOptions options)) {
            throw new RuntimeException("failed to initialize data field " + dataField.getName() + " options");
        }
        var dataRelCollection = this.resolver.loadCollection(options.getCollectionId());
        if (dataRelCollection == null) {
            throw new RuntimeException("failed to load collection " + options.getCollectionId() + " from data field " + dataField.getName());
        }

        List<String> dataRelIds = null;
        if (this.resolver.getRequestInfo() != null && !this.resolver.getRequestInfo().getData().isEmpty()) {
            dataRelIds = ListUtil.toUniqueStringList(this.resolver.getRequestInfo().getData().get(dataField.getName()));
        }
        if (dataRelIds == null || dataRelIds.isEmpty()) {
            return ResolverResult.builder().identifier("NULL").build();
        }

        this.activeCollectionName = dataRelCollection.getName();
        this.activeTableAlias = columnify("__data_" + this.activeCollectionName + "_" + dataField.getName());

        // join the data rel collection to the main collection
        this.resolver.registerJoin(
                this.activeCollectionName,
                this.activeTableAlias,
                Expression.in(String.format("%s.id", this.activeTableAlias),
                        dataRelIds));

        if (options.isMultiple()) {
            this.withMultiMatch = true;
        }

        // join the data rel collection to the multi-match subquery
        this.multiMatchActiveTableAlias = columnify("__data_mm_" + dataRelCollection.getName() + "_" + dataField.getName());
        this.multiMatch.getJoins().add(
                new Join(this.activeCollectionName,
                        this.multiMatchActiveTableAlias,
                        Expression.in(String.format("%s.id", this.multiMatchActiveTableAlias), dataRelIds)));

        // leave only the data relation fields
        // aka. @request.data.someRel.fieldA.fieldB -> fieldA.fieldB
        this.activeProps = this.activeProps.subList(3, this.activeProps.size());

        return this.processActiveProps();
    }

    private ResolverResult processRequestAuthField() {
        // plain auth field
        // ---
        if (existInArray(this.fieldName, plainRequestAuthFields)) {
            return this.resolver.resolveStaticRequestField(this.activeProps.subList(1, this.activeProps.size()));
        }

        // resolve the auth collection field
        // ---
        if (this.resolver.getRequestInfo() == null || this.resolver.getRequestInfo().getAuthRecord() == null || this.resolver.getRequestInfo().getAuthRecord().getCollection() == null) {
            return ResolverResult.builder().identifier("NULL").build();
        }
        var collection = this.resolver.getRequestInfo().getAuthRecord().getCollection();
        this.resolver.getLoadedCollections().add(collection);

        this.activeCollectionName = collection.getName();
        this.activeTableAlias = "__auth_" + columnify(this.activeCollectionName);

        // join the auth collection to the main query
        this.resolver.registerJoin(
                columnify(this.activeCollectionName),
                this.activeTableAlias,
                Expression.newHashExpr(Map.of(this.activeTableAlias + ".id", this.resolver.getRequestInfo().getAuthRecord().getId())));

        // join the auth collection to the multi-match subquery
        this.multiMatchActiveTableAlias = "__mm_" + this.activeTableAlias;
        this.multiMatch.getJoins().add(new Join(columnify(this.activeCollectionName),
                this.multiMatchActiveTableAlias,
                Expression.newHashExpr(Map.of(this.multiMatchActiveTableAlias + ".id", this.resolver.getRequestInfo().getAuthRecord().getId()))));

        // leave only the auth relation fields
        // aka. @request.auth.fieldA.fieldB -> fieldA.fieldB
        this.activeProps = this.activeProps.subList(2, this.activeProps.size());
        return this.processActiveProps();
    }


    private ResolverResult processCollectionField() {
        if (this.activeProps.size() < 3) {
            throw new RuntimeException("invalid @collection field path in " + this.fieldName);
        }

        // nameOrId or nameOrId:alias
        var collectionParts = this.activeProps.get(1).split(":", 2);

        CollectionModel collection = this.resolver.loadCollection(collectionParts[0]);

        if (collection == null) {
            throw new RuntimeException("failed to load collection " + activeProps.get(1) + " from field path " + this.fieldName);
        }
        this.activeCollectionName = collection.getName();

        if (collectionParts.length == 2 && !collectionParts[1].isEmpty()) {
            this.activeTableAlias = columnify("__collection_alias_" + collectionParts[1]);
        } else {
            this.activeTableAlias = columnify("__collection_" + this.activeCollectionName);
        }

        this.withMultiMatch = true;

        // join the collection to the main query
        this.resolver.registerJoin(columnify(collection.getName()), this.activeTableAlias, newExpr("1=1"));

        // join the collection to the multi-match subquery
        this.multiMatchActiveTableAlias = "__mm" + this.activeTableAlias;
        this.multiMatch.getJoins().add(new Join(collection.getName(), this.multiMatchActiveTableAlias, newExpr("1=1")));
        // leave only the collection fields
        // aka. @collection.someCollection.fieldA.fieldB -> fieldA.fieldB
        this.activeProps = this.activeProps.subList(2, this.activeProps.size());
        return this.processActiveProps();
    }

    private ResolverResult processActiveProps() {
        var totalProps = this.activeProps.size();

        for (int i = 0; i < totalProps; i++) {
            var collection = this.resolver.loadCollection(this.activeCollectionName);
            if (collection == null) {
                throw new RuntimeException("failed to load collection " + this.activeCollectionName + " from field path " + this.fieldName);
            }
            var prop = this.activeProps.get(i);
            //last prop
            if (i == totalProps - 1) {
                // system field, aka. internal model prop
                // (always available but not part of the collection schema)
                // -------------------------------------------------------
                if (existInArray(prop, resolvableSystemFieldNames(collection))) {
                    var result = ResolverResult.builder().identifier(String.format("%s.%s", this.activeTableAlias, columnify(prop)));

                    // allow querying only auth records with emails marked as public
                    if (prop.equals(FieldName.Email) && !this.allowHiddenFields) {
                        result.afterBuild(expr ->
                                Expression.enclose(
                                        Expression.and(
                                                expr, newExpr(String.format("%s.%s = TRUE", this.activeTableAlias, FieldName.EmailVisibility))
                                        )
                                ));
                    }

                    if (this.withMultiMatch) {
                        this.multiMatch.setValueIdentifier(String.format("%s.%s", this.multiMatchActiveTableAlias, columnify(prop)));
                        result.multiMatchSubQuery(this.multiMatch);
                    }

                    return result.build();
                }

                var arr = splitModifier(prop);
                var name = arr[0];
                var modifier = arr[1];

                var field = collection.getSchema().getFieldByName(name);
                if (field == null) {
                    if (this.nullifyMisingField) {
                        return ResolverResult.builder().identifier("NULL").build();
                    }
                    throw new RuntimeException("unknown field " + name);
                }
                var cleanFieldName = columnify(field.getName());

                // arrayable fields with ":length" modifier
                // -------------------------------------------------------
                if (LengthModifier.equals(modifier) && existInArray(field.getType(), ArraybleFieldTypes())) {
                    var jePair = this.activeTableAlias + "." + cleanFieldName;

                    var result = ResolverResult.builder().identifier(DbUtil.jsonArrayLength(jePair));

                    if (this.withMultiMatch) {
                        var jePair2 = this.multiMatchActiveTableAlias + "." + cleanFieldName;
                        this.multiMatch.setValueIdentifier(DbUtil.jsonArrayLength(jePair2));
                        result.multiMatchSubQuery(this.multiMatch);
                    }
                    return result.build();
                }

                // arrayable fields with ":each" modifier
                // -------------------------------------------------------
                if (EachModifier.equals(modifier) && existInArray(field.getType(), ArraybleFieldTypes())) {
                    var jePair = this.activeTableAlias + "." + cleanFieldName;
                    var jeAlias = this.activeTableAlias + "_" + cleanFieldName + "_je";
                    this.resolver.registerJoin(DbUtil.jsonEach(jePair), jeAlias, newExpr("1=1"));
                    var result = ResolverResult.builder().identifier(String.format("%s.value", jeAlias));

                    if (!(field.getOptions() instanceof MultiValuer options)) {
                        throw new RuntimeException("field " + prop + " options are not initialized or doesn't multivaluer arrayable operations");
                    }

                    if (options.isMultiple()) {
                        this.withMultiMatch = true;
                    }

                    if (this.withMultiMatch) {
                        var jePair2 = this.multiMatchActiveTableAlias + "." + cleanFieldName;
                        var jeAlias2 = this.multiMatchActiveTableAlias + "_" + cleanFieldName + "_je";

                        this.multiMatch.getJoins().add(new Join(DbUtil.jsonEach(jePair2), jeAlias2, newExpr("1=1")));
                        this.multiMatch.setValueIdentifier(String.format("%s.value", jeAlias2));
                        result.multiMatchSubQuery(this.multiMatch);
                    }
                    return result.build();
                }

                // default
                // -------------------------------------------------------

                var result = ResolverResult.builder().identifier(this.activeTableAlias + "." + cleanFieldName);

                if (this.withMultiMatch) {
                    this.multiMatch.setValueIdentifier(String.format("%s.%s", this.multiMatchActiveTableAlias, cleanFieldName));
                    result.multiMatchSubQuery(this.multiMatch);
                }

                // wrap in json_extract to ensure that top-level primitives
                // stored as json work correctly when compared to their SQL equivalent
                // (https://github.com/pocketbase/pocketbase/issues/4068)
                if (field.getType().equals(FieldType.Json)) {
                    result.noCoalesce(true);
                    result.identifier(DbUtil.jsonExtract(this.activeTableAlias + "." + cleanFieldName, ""));
                    if (this.withMultiMatch) {
                        this.multiMatch.setValueIdentifier(DbUtil.jsonExtract(this.multiMatchActiveTableAlias + "." + cleanFieldName, ""));
                    }
                }

                return result.build();
            }

            var field = collection.getSchema().getFieldByName(prop);

            // json field -> treat the rest of the props as json path
            if (field != null && field.getType().equals(FieldType.Json)) {
                StringBuilder jsonPath = new StringBuilder();
                var supProps = this.activeProps.subList(i + 1, totalProps);
                for (int j = 0; j < supProps.size(); j++) {
                    var p = supProps.get(j);
                    if (NumberUtil.isNumber(p)) {
                        jsonPath.append("[").append(columnify(p)).append("]");
                    } else {
                        if (j > 0) {
                            jsonPath.append(".");
                        }
                        jsonPath.append(columnify(p));
                    }
                }
                var jsonPathStr = jsonPath.toString();
                var result = ResolverResult.builder().noCoalesce(true).identifier(DbUtil.jsonExtract(this.activeTableAlias + "." + columnify(prop), jsonPathStr));

                if (this.withMultiMatch) {
                    this.multiMatch.setValueIdentifier(DbUtil.jsonExtract(this.multiMatchActiveTableAlias + "." + columnify(prop), jsonPathStr));
                    result.multiMatchSubQuery(this.multiMatch);
                }

                return result.build();
            }


            if (i >= maxNestedRels) {
                throw new RuntimeException("max nested relations reached for field " + prop);
            }

            // check for back relation (eg. yourCollection_via_yourRelField)
            // -----------------------------------------------------------
            if (field == null) {
                Matcher matcher = IndirectExpandRegexPattern.matcher(prop);
                if (!matcher.find()) {
                    if (this.nullifyMisingField) {
                        return ResolverResult.builder().identifier("NULL").build();
                    }
                    throw new RuntimeException("failed to resolve field " + prop);
                }

                var backCollection = this.resolver.loadCollection(matcher.group(1));
                if (backCollection == null) {
                    if (this.nullifyMisingField) {
                        return ResolverResult.builder().identifier("NULL").build();
                    }
                    throw new RuntimeException("failed to load back relation field " + prop + " collection");
                }

                var backField = backCollection.getSchema().getFieldByName(matcher.group(2));
                if (backField == null) {
                    if (this.nullifyMisingField) {
                        return ResolverResult.builder().identifier("NULL").build();
                    }
                    throw new RuntimeException("missing back relation field " + matcher.group(2));
                }
                if (!FieldType.Relation.equals(backField.getType())) {
                    throw new RuntimeException("invalid back relation field " + matcher.group(2));
                }

                backField.initOptions();
                if (!(backField.getOptions() instanceof RelationOptions backFieldOptions)) {
                    throw new RuntimeException("failed to initialize back relation field " + backField.getName() + " options");
                }
                if (!backFieldOptions.getCollectionId().equals(collection.getId())) {
                    throw new RuntimeException("invalid back relation field " + backField.getName() + " collection reference");
                }

                // join the back relation to the main query
                // ---
                var cleanProp = columnify(prop);
                var cleanBackFieldName = columnify(backField.getName());
                var newTableAlias = this.activeTableAlias + "_" + cleanProp;
                var newCollectionName = columnify(backCollection.getName());

                var isBackRelMultiple = backFieldOptions.isMultiple();
                if (!isBackRelMultiple) {
                    // additionally check if the rel field has a single column unique index
                    isBackRelMultiple = !hasSingleColumnUniqueIndex(backField.getName(), backCollection.getIndexes());
                }

                if (!isBackRelMultiple) {
                    this.resolver.registerJoin(newCollectionName, newTableAlias,
                            newExpr(String.format("%s.%s=%s.id", newTableAlias, cleanBackFieldName, this.activeTableAlias)));
                } else {
                    var jeAlias = this.activeTableAlias + "_" + cleanProp + "_je";

                    this.resolver.registerJoin(String.format("(SELECT * FROM %s,%s %s)", newCollectionName, DbUtil.jsonEach(newCollectionName + "." + cleanBackFieldName), jeAlias),
                            newTableAlias, newExpr(String.format("`%s`.`value` = `%s`.`id`", newTableAlias, this.activeTableAlias)));

                    /*this.resolver.registerJoin(newCollectionName, newTableAlias,
                            newExpr(String.format("%s.id IN (SELECT %s.value FROM %s `%s`)",
                                    this.activeTableAlias,
                                    jeAlias,
                                    DbUtil.jsonEach(newTableAlias + "." + cleanBackFieldName),
                                    jeAlias
                            )));*/
                }

                this.activeCollectionName = newCollectionName;
                this.activeTableAlias = newTableAlias;
                // ---

                // join the back relation to the multi-match subquery
                // ---

                if (isBackRelMultiple) {
                    this.withMultiMatch = true;
                }

                var newTableAlias2 = this.multiMatchActiveTableAlias + "_" + cleanProp;

                if (!isBackRelMultiple) {
                    this.multiMatch.getJoins().add(new Join(newCollectionName, newTableAlias2,
                            newExpr(String.format("%s.%s = %s.id", newTableAlias2, cleanBackFieldName, this.multiMatchActiveTableAlias))));
                } else {
                    var jeAlias2 = this.multiMatchActiveTableAlias + "_" + cleanProp + "_je";


                    this.multiMatch.getJoins().add(new Join(String.format("(SELECT * FROM %s,%s %s)", newCollectionName, DbUtil.jsonEach(newCollectionName + "." + cleanBackFieldName), jeAlias2),
                            newTableAlias2, newExpr(String.format("`%s`.`value` = `%s`.`id`", newTableAlias2, this.multiMatchActiveTableAlias))));
                   /* this.multiMatch.getJoins().add(new Join(newCollectionName, newTableAlias2,
                            newExpr(String.format("%s.id IN (SELECT %s.value FROM %s `%s`)",
                                    this.multiMatchActiveTableAlias,
                                    jeAlias2,
                                    DbUtil.jsonEach(newTableAlias2 + "." + cleanBackFieldName),
                                    jeAlias2))));*/
                }

                this.multiMatchActiveTableAlias = newTableAlias2;
                // ----

                continue;
            }
            //-------------

            // check for direct relation
            if (!FieldType.Relation.equals(field.getType())) {
                throw new RuntimeException("field " + prop + " is not a valid relation");
            }

            // join the relation to the main query
            // ----
            field.initOptions();
            if (!(field.getOptions() instanceof RelationOptions)) {
                throw new RuntimeException("failed to initialize field " + prop + " options");
            }
            var options = (RelationOptions) field.getOptions();

            var relCollection = this.resolver.loadCollection(options.getCollectionId());
            if (relCollection == null) {
                throw new RuntimeException("failed to load field " + prop + " collection");
            }

            var cleanFieldName = columnify(field.getName());
            var prefixedFieldName = this.activeTableAlias + "." + cleanFieldName;
            var newTableAlias = this.activeTableAlias + "_" + cleanFieldName;
            var newCollectionName = relCollection.getName();

            if (!options.isMultiple()) {
                this.resolver.registerJoin(
                        columnify(newCollectionName),
                        newTableAlias,
                        newExpr(String.format("%s.id = %s", newTableAlias, prefixedFieldName))
                );
            } else {
                var jeAlias = this.activeTableAlias + "_" + cleanFieldName + "_je";
                this.resolver.registerJoin(DbUtil.jsonEach(prefixedFieldName), jeAlias, newExpr("1=1"));
                this.resolver.registerJoin(columnify(newCollectionName), newTableAlias,
                        newExpr(String.format("`%s`.`id` = `%s`.`value`", newTableAlias, jeAlias)));
            }

            this.activeCollectionName = newCollectionName;
            this.activeTableAlias = newTableAlias;
            // ---

            // join the relation to the multi-match subquery
            // ---
            if (options.isMultiple()) {
                this.withMultiMatch = true;  // enable multimatch if not already
            }

            var newTableAlias2 = this.multiMatchActiveTableAlias + "_" + cleanFieldName;
            var prefixedFieldName2 = this.multiMatchActiveTableAlias + "." + cleanFieldName;

            if (!options.isMultiple()) {
                this.multiMatch.getJoins().add(new Join(columnify(newCollectionName), newTableAlias2,
                        newExpr(String.format("%s.id = %s", newTableAlias2, prefixedFieldName2))));
            } else {
                var jeAlias2 = this.multiMatchActiveTableAlias + "_" + cleanFieldName + "_je";
                this.multiMatch.getJoins().add(new Join(DbUtil.jsonEach(prefixedFieldName2), jeAlias2,
                        newExpr("1=1"))); // jsonTable 后的 On操作
                this.multiMatch.getJoins().add(new Join(columnify(newCollectionName), newTableAlias2,
                        newExpr(String.format("%s.id = %s.value", newTableAlias2, jeAlias2))));
            }

            this.multiMatchActiveTableAlias = newTableAlias2;
            //----
        }


        throw new RuntimeException("failed to resolve field " + this.fieldName);
    }

    private String[] resolvableSystemFieldNames(CollectionModel collection) {
        var result = new ArrayList<String>(Arrays.asList(baseModelFieldNames));

        if (collection.isAuth()) {
            result.add(FieldName.Username);
            result.add(FieldName.Verified);
            result.add(FieldName.EmailVisibility);
            result.add(FieldName.Email);
        }
        return result.toArray(new String[0]);
    }

    private void prepare() {
        this.activeProps = Arrays.asList(this.fieldName.split("\\.", -1));
        this.activeCollectionName = this.resolver.getBaseCollection().getName();
        this.activeTableAlias = columnify(this.activeCollectionName);

        this.allowHiddenFields = this.resolver.isAllowHiddenFields();
        // always allow hidden fields since the @.* filter is a system one
        if (this.activeProps.get(0).equals("@collection")
                || this.activeProps.get(0).equals("@request")) {
            this.allowHiddenFields = true;
        }

        //为了向后兼容性和与所有 @request.* 过滤字段和类型的一致性，启用对缺失的 @request.* 字段的忽略标志
        // enable the ignore flag for missing @request.* fields for backward
        // compatibility and consistency with all @request.* filter fields and types
        this.nullifyMisingField = this.activeProps.get(0).equals("@request");

        this.multiMatch = new MultiMatchSubquery(this.activeTableAlias);
        this.multiMatch.setFromTableName(columnify(this.activeCollectionName));
        this.multiMatch.setFromTableAlias("__mm_" + this.activeTableAlias);
        this.multiMatchActiveTableAlias = this.multiMatch.getFromTableAlias();
        this.withMultiMatch = false;
    }

    public static void main(String[] args) {
        String a = "a:b:c";
        Arrays.stream(a.split(":", 2)).forEach(System.out::println);
        //@collection.someCollection.fieldA.fieldB -> fieldA.fieldB
        var l = new ArrayList<>(List.of("@collection", "someCollection", "fieldA", "fieldB"));
        System.out.println(l.subList(2, l.size()));

        String input = "Hello, World! zouqiang Hello,zzz,World!";
        String regex = "(H\\w+), (W\\w+)!";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            System.out.println("Full match: " + matcher.group(0));
            System.out.println("First group: " + matcher.group(1));
            System.out.println("Second group: " + matcher.group(2));
        } else {
            System.out.println("No match found.");
        }
    }
}