package com.picobase.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.exception.BadRequestException;
import com.picobase.interceptor.InterceptorFunc;
import com.picobase.interceptor.InterceptorNextFunc;
import com.picobase.interceptor.Interceptors;
import com.picobase.json.PbJsonTemplate;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.CollectionAuthOptions;
import com.picobase.model.schema.fieldoptions.CollectionViewOptions;
import com.picobase.model.schema.fieldoptions.RelationOptions;
import com.picobase.persistence.model.Index;
import com.picobase.persistence.resolver.RecordFieldResolver;
import com.picobase.search.SearchFilter;
import com.picobase.validator.Err;
import com.picobase.validator.Errors;
import com.picobase.validator.RequiredRule;
import com.picobase.validator.RuleFunc;

import java.util.*;

import static com.picobase.model.validators.Validators.uniqueId;
import static com.picobase.util.PbConstants.*;
import static com.picobase.util.PbConstants.CollectionType.*;
import static com.picobase.util.PbConstants.FieldType.Relation;
import static com.picobase.validator.Err.newError;
import static com.picobase.validator.Err.newErrors;
import static com.picobase.validator.Validation.*;


public class CollectionUpsert {

    private final CollectionMapper mapper = PbUtil.findMapper(CollectionModel.class);
    private String id;
    private String name;
    private String type;
    private boolean system;
    private Schema schema;
    private List<String> indexes;
    private String listRule;
    private String viewRule;
    private String createRule;
    private String updateRule;
    private String deleteRule;
    private Map<String, Object> options;
    private CollectionModel collection;
    ;
    private PbJsonTemplate jsonTemplate;

    public CollectionUpsert() {

    }

    public CollectionUpsert(CollectionModel collection) {
        this.collection = collection;
        this.id = collection.getId();
        this.name = collection.getName();
        this.type = collection.getType();
        this.system = collection.isSystem();
        this.schema = collection.getSchema();
        this.indexes = collection.getIndexes();
        this.listRule = collection.getListRule();
        this.viewRule = collection.getViewRule();
        this.createRule = collection.getCreateRule();
        this.updateRule = collection.getUpdateRule();
        this.deleteRule = collection.getDeleteRule();
        this.options = collection.getOptions();
        this.jsonTemplate = PbManager.getPbJsonTemplate();
    }

    public String getId() {
        return id;
    }

    public CollectionUpsert setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CollectionUpsert setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public CollectionUpsert setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isSystem() {
        return system;
    }

    public CollectionUpsert setSystem(boolean system) {
        this.system = system;
        return this;
    }

    public Schema getSchema() {
        return schema;
    }

    public CollectionUpsert setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public CollectionUpsert setIndexes(List<String> indexes) {
        this.indexes = indexes;
        return this;
    }

    public String getListRule() {
        return listRule;
    }

    public CollectionUpsert setListRule(String listRule) {
        this.listRule = listRule;
        return this;
    }

    public String getViewRule() {
        return viewRule;
    }

    public CollectionUpsert setViewRule(String viewRule) {
        this.viewRule = viewRule;
        return this;
    }

    public String getCreateRule() {
        return createRule;
    }

    public CollectionUpsert setCreateRule(String createRule) {
        this.createRule = createRule;
        return this;
    }

    public String getUpdateRule() {
        return updateRule;
    }

    public CollectionUpsert setUpdateRule(String updateRule) {
        this.updateRule = updateRule;
        return this;
    }

    public String getDeleteRule() {
        return deleteRule;
    }

    public CollectionUpsert setDeleteRule(String deleteRule) {
        this.deleteRule = deleteRule;
        return this;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public CollectionUpsert setOptions(Map<String, Object> options) {
        this.options = options;
        return this;
    }

    public CollectionModel getCollection() {
        return collection;
    }

    public CollectionUpsert setCollection(CollectionModel collection) {
        this.collection = collection;
        return this;
    }

    /**
     * 校验当前 Collection 信息
     *
     * @param isCreat 区分是否是创建或是更新
     * @return
     */
    public Errors validate(boolean isCreat) {
        boolean isAuth = Objects.equals(this.type, Auth);
        boolean isView = Objects.equals(this.type, View);
        boolean isNew = isCreat;

        return PbUtil.validate(this,
                field(CollectionUpsert::getId, when(isNew, length(DEFAULT_ID_LENGTH, DEFAULT_ID_LENGTH), match(ID_REGEX_P), by(uniqueId(this.collection.tableName())))
                        .otherwise(in(this.collection.getId()))),
                field(CollectionUpsert::isSystem, by(ensureNoSystemFlagChange(isNew))),
                field(CollectionUpsert::getType, required, in(Base, Auth, View), by(ensureNoTypeChange(isNew))),
                field(CollectionUpsert::getName, required, length(1, 255), match(COLLECTION_NAME_P), by(ensureNoSystemNameChange(isNew)), by(checkUniqueName()), by(checkForVia())),
                field(CollectionUpsert::getSchema, by(checkMinSchemaFields()), by(ensureNoSystemFieldsChange()), by(ensureNoFieldsTypeChange()), by(checkRelationFields()), when(isAuth, by(ensureNoAuthFieldName()))),
                field(CollectionUpsert::getListRule, by(checkRule())),
                field(CollectionUpsert::getViewRule, by(checkRule())),
                field(CollectionUpsert::getCreateRule, when(isView, Nil), by(checkRule())),
                field(CollectionUpsert::getUpdateRule, when(isView, Nil), by(checkRule())),
                field(CollectionUpsert::getDeleteRule, when(isView, Nil), by(checkRule())),
                field(CollectionUpsert::getIndexes, by(checkIndexes())),
                field(CollectionUpsert::getOptions, by(checkOptions()))
        );
    }


    private RuleFunc checkUniqueName() {
        return value -> {
            // ensure unique collection name
            if (!mapper.isCollectionNameUnique(this.name, this.collection.getId())) {
                return newError("validation_collection_name_exists", "Collection name must be unique (case insensitive).");
            }
            // ensure that the collection name doesn't collide with the id of any collection
            if (PbUtil.findById(CollectionModel.class, this.name) != null) {
                return newError("validation_collection_name_id_duplicate", "The name must not match an existing collection id.");
            }
            return null;
        };
    }

    public RuleFunc checkRelationFields() {
        return value -> {
            var errors = newErrors();
            Schema v = (Schema) value;
            for (int i = 0; i < v.getFields().size(); i++) {
                SchemaField field = v.getFields().get(i);

                if (!Objects.equals(field.getType(), Relation)) {
                    continue;
                }

                field.initOptions();
                RelationOptions options = (RelationOptions) field.getOptions();
                if (options == null) {
                    errors.put(String.valueOf(i),
                            newErrors().put("options",
                                    newError(
                                            "validation_schema_invalid_relation_field_options",
                                            "The relation field has invalid field options."
                                    )));
                    return errors;
                }

                SchemaField oldField = this.getSchema().getFieldById(field.getId());
                if (oldField != null) {
                    RelationOptions oldOptions = (RelationOptions) oldField.getOptions();
                    if (oldOptions != null && !oldOptions.getCollectionId().equals(options.getCollectionId())) {
                        errors.put(String.valueOf(i),
                                newErrors().put("options",
                                        newErrors().put("collectionId",
                                                newError(
                                                        "validation_field_relation_change",
                                                        "The relation collection cannot be changed."
                                                )
                                        )
                                ));
                        return errors;
                    }
                }

                CollectionModel relCollection = mapper.findCollectionByNameOrId(options.getCollectionId());
                if (relCollection == null || !relCollection.getId().equals(options.getCollectionId())) {
                    errors.put(String.valueOf(i),
                            newErrors().put("options",
                                    newErrors().put("collectionId",
                                            newError(
                                                    "validation_field_invalid_relation",
                                                    "The relation collection doesn't exist."
                                            )
                                    )
                            ));
                    return errors;
                }

                if (!this.type.equals(View) && relCollection.isView()) {
                    errors.put(String.valueOf(i),
                            newErrors().put("options",
                                    newErrors().put("collectionId",
                                            newError(
                                                    "validation_field_non_view_base_relation_collection",
                                                    "Non view collections are not allowed to have a view relation."
                                            )
                                    )
                            ));
                    return errors;
                }
            }

            return null;
        };
    }

    private RuleFunc ensureNoAuthFieldName() {
        return value -> {
            var errors = newErrors();
            Schema v = (Schema) value;
            if (!Objects.equals(this.type, Auth)) {
                return null;  // not an auth collection
            }

            List<String> authFieldNameList = new ArrayList<>(Arrays.asList(authFieldNames));
            // exclude the meta RecordUpsert form fields
            authFieldNameList.add("password");
            authFieldNameList.add("passwordConfirm");
            authFieldNameList.add("oldPassword");

            List<SchemaField> fields = v.getFields();
            for (int i = 0; i < fields.size(); i++) {
                if (CollUtil.contains(authFieldNameList, fields.get(i).getName())) {
                    errors.put(String.valueOf(i),
                            newErrors().put("name", newError("validation_reserved_auth_field_name", "The field name is reserved and cannot be used.")));
                }
            }
            return errors;
        };
    }


    private RuleFunc ensureNoSystemFlagChange(boolean isNew) {
        return value -> {
            if (!isNew && (Boolean) value != this.getCollection().isSystem()) {
                return newError("validation_collection_system_flag_change", "System collection state cannot be changed.");
            }
            return null;
        };
    }

    private RuleFunc ensureNoTypeChange(boolean isNew) {
        return value -> {
            if (!isNew && !Objects.equals(value, this.collection.getType())) {
                return newError("validation_collection_type_change", "Collection type cannot be changed.");
            }
            return null;
        };
    }

    private RuleFunc ensureNoSystemNameChange(boolean isNew) {
        return value -> {
            if (!isNew && this.collection.isSystem() && !Objects.equals(value, this.collection.getName())) {
                return newError("validation_collection_system_name_change", "System collections cannot be renamed.");
            }
            return null;
        };
    }

    private RuleFunc checkMinSchemaFields() {
        return value -> {
            switch (this.type) {
                case Auth:
                case View:
                    return null;
                default:
                    if (this.getSchema().getFields().size() == 0) {
                        return RequiredRule.ErrRequired;
                    }
            }
            return null;
        };
    }

    private RuleFunc checkForVia() {
        return value -> {
            if (Objects.equals("", this.name)) {
                return null;
            }
            if (StrUtil.contains(((String) value).toLowerCase(), "_via_")) {
                return newError("validation_invalid_name", "The name of the collection cannot contain '_via_'.");
            }
            return null;
        };
    }

    private RuleFunc ensureNoSystemFieldsChange() {
        return value -> {
            for (SchemaField oldField : this.collection.getSchema().getFields()) {
                if (!oldField.isSystem()) {
                    continue;
                }

                SchemaField newField = ((Schema) value).getFieldById(oldField.getId());
                if (null == newField || !Objects.equals(oldField, newField)) {
                    return newError("validation_system_field_change", "System fields cannot be deleted or changed.");
                }
            }
            return null;
        };
    }

    private RuleFunc ensureNoFieldsTypeChange() {
        return value -> {
            var errors = newErrors();
            List<SchemaField> fields = ((Schema) value).getFields();
            for (int i = 0; i < fields.size(); i++) {
                SchemaField oldField = this.collection.getSchema().getFieldById(fields.get(i).getId());
                if (null != oldField && !Objects.equals(oldField.getType(), fields.get(i).getType())) {
                    errors.put(String.valueOf(i), newError("validation_field_type_change", "Field type cannot be changed."));
                }
            }

            return errors;
        };
    }

    private RuleFunc checkIndexes() {
        return value -> {
            List<String> indexs = (List<String>) value;
            if (Objects.equals(this.type, View) && indexs.size() > 0) {
                return newError("validation_indexes_not_supported",
                        "The collection doesn't support indexes.");
            }

            var errors = newErrors();
            for (int i = 0; i < indexs.size(); i++) {
                Index parsed = Index.parseIndex(indexs.get(i));
                if (!parsed.isValid()) {
                    errors.put(String.valueOf(i), newError("validation_invalid_index_expression",
                            "Invalid CREATE INDEX expression."));
                }
                return errors;
            }
            return null;
        };
    }


    private RuleFunc checkOptions() {
        return value -> {

            switch (this.type) {
                case Auth -> {
                    String raw = jsonTemplate.toJsonString(this.options);

                    // decode into the provided result
                    CollectionAuthOptions authOptions = jsonTemplate.parseJsonToObject(raw, CollectionAuthOptions.class);

                    // check the generic validations
                    Errors err = authOptions.validate();
                    if (null != err) {
                        return err;
                    }

                    // additional form specific validations
                    Err error = this.checkRule().apply(authOptions.getManageRule());
                    if (null != error) {
                        return error;
                    }
                }
                case View -> {
                    String raw = jsonTemplate.toJsonString(this.options);
                    CollectionViewOptions viewOptions = jsonTemplate.parseJsonToObject(raw, CollectionViewOptions.class);

                    // check the generic validations
                    Errors err = viewOptions.validate();
                    if (null != err) {
                        return err;
                    }

                    // check the query option
                    try {
                        mapper.createViewSchema(viewOptions.getQuery());
                    } catch (Exception e) {
                        return newError("validation_invalid_view_query", String.format("Invalid query - %s", e.getMessage()));
                    }
                }
            }

            return null;
        };
    }

    private RuleFunc checkRule() {
        return value -> {
            if (StrUtil.isEmpty((CharSequence) value)) {
                return null; // nothing to check
            }

            /**
             * dummy := *form.collection 这行代码将form结构体中的collection字段的值复制给dummy变量。由于form.collection是一个指针，因此*form.collection表示对collection字段指针的解引用，即获取指针指向的实际值。
             * 	dummy.Type = form.Type
             * 	dummy.Schema = form.Schema
             * 	dummy.System = form.System
             * 	dummy.Options = form.Options
             *
             */
            CollectionModel dummy = new CollectionModel();
            dummy.setId(this.collection.getId());
            dummy.setName(this.collection.getName()); //复制

            dummy.setType(this.type);
            dummy.setSchema(this.schema);
            dummy.setSystem(this.system);
            dummy.setOptions(this.options);
            try {
                RecordFieldResolver resolver = new RecordFieldResolver(dummy, null, true);
                new SearchFilter((String) value).buildExpr(resolver);
            } catch (Exception e) {
                return newError("validation_invalid_rule", "Invalid filter rule. Raw error: " + e.getMessage());
            }
            return null;
        };
    }


    public CollectionModel submit(boolean isCreate, InterceptorFunc<CollectionModel, CollectionModel>... postEventFunc) {
        CollectionModel collection = this.getCollection();
        Errors errors = this.validate(isCreate);
        if (errors != null) {
            throw new BadRequestException(errors);
        }

        if (isCreate) {
            //type can be set only on create
            collection.setType(this.getType());
            // system flag can be set only on create
            collection.setSystem(this.isSystem());

            // id can be set only on create
            if (StrUtil.isNotEmpty(this.getId())) {
                collection.setId(this.getId());
            } else {
                collection.refreshId();
            }

            collection.refreshCreated();
            collection.refreshUpdated();
        } else {
            collection.refreshUpdated();
        }


        // system collections cannot be renamed
        if (isCreate || !collection.isSystem()) {
            collection.setName(this.getName());
        }

        // view schema is autogenerated on save and cannot have indexes
        if (!collection.isView()) {
            collection.setSchema(this.getSchema());

            // normalize indexes format
            collection.setIndexes(this.getIndexes());
        }

        collection.setCreateRule(this.getCreateRule());
        collection.setDeleteRule(this.getDeleteRule());
        collection.setUpdateRule(this.getUpdateRule());
        collection.setListRule(this.getListRule());
        collection.setViewRule(this.getViewRule());
        collection.setOptions(this.getOptions());


        return Interceptors.run(this.collection, saveCollectionNextFunc(isCreate), postEventFunc);
    }

    private InterceptorNextFunc<CollectionModel, CollectionModel> saveCollectionNextFunc(boolean isCreate) {
        return (col) -> {
            switch (col.getType()) {
                case View -> mapper.saveViewCollection(col, null);
                default -> {

                    if (isCreate) {
                        mapper.syncRecordTableSchema(col, null);
                        PbUtil.save(col);
                    } else {
                        mapper.syncRecordTableSchema(col, mapper.findCollectionByNameOrId(col.getId()));
                        PbUtil.updateById(col.getId(), col);
                    }

                }
            }

            // trigger an update for all views with changed schema as a result of the current collection save
            // (ignoring view errors to allow users to update the query from the UI)
            try {
                mapper.resaveViewsWithChangedSchema(col.getId());
            } catch (Exception e) {
                e.printStackTrace();
                //ignore
            }

            return col;
        };
    }

}
