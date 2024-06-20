package com.picobase.model;


import com.picobase.PbManager;
import com.picobase.json.PbJsonTemplate;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.fieldoptions.CollectionAuthOptions;
import com.picobase.model.schema.fieldoptions.CollectionBaseOptions;
import com.picobase.model.schema.fieldoptions.CollectionViewOptions;
import com.picobase.util.PbConstants;

import java.util.*;

import static com.picobase.util.PbConstants.CollectionType.*;


public class CollectionModel extends BaseModel implements Model {


    private String name;
    private String type;
    private boolean system;

    private Schema schema = Schema.newSchema();

    private List<String> indexes;

    private String listRule;
    private String viewRule;
    private String createRule;
    private String updateRule;
    private String deleteRule;

    private Map<String, Object> options;

    public CollectionModel() {

    }

    public CollectionModel(CollectionModel source) {
        super.setId(source.getId());
        super.setCreated(source.getCreated());
        super.setUpdated(source.getUpdated());
        this.name = source.name;
        this.type = source.type;
        this.system = source.system;
        this.schema = Schema.newSchema(source.schema.getFields());
        this.indexes = new ArrayList<>(source.indexes);
        this.listRule = source.listRule;
        this.viewRule = source.viewRule;
        this.createRule = source.createRule;
        this.updateRule = source.updateRule;
        this.deleteRule = source.deleteRule;
        this.options = new HashMap<>(source.options);
    }

    @Override
    public String tableName() {
        return PbConstants.TableName.COLLECTION;
    }

    public String baseFilesPath() {
        return this.getId();
    }

    // IsBase checks if the current collection has "base" type.
    public boolean isBase() {
        return Objects.equals(this.type, Base);
    }

    // IsAuth checks if the current collection has "auth" type.
    public boolean isAuth() {
        return Objects.equals(this.type, Auth);
    }

    // IsView checks if the current collection has "view" type.
    public boolean isView() {
        return Objects.equals(this.type, View);
    }


    public CollectionBaseOptions baseOptions() {
        return decodeOptions(CollectionBaseOptions.class);
    }

    public CollectionAuthOptions authOptions() {
        return decodeOptions(CollectionAuthOptions.class);
    }

    public CollectionViewOptions viewOptions() {
        return decodeOptions(CollectionViewOptions.class);
    }


    public <T> T decodeOptions(Class<T> clazz) {
        // raw serialize

        PbJsonTemplate pbJsonTemplate = PbManager.getPbJsonTemplate();

        String raw = pbJsonTemplate.toJsonString(options);

        return pbJsonTemplate.parseJsonToObject(raw, clazz);

    }

    public String getName() {
        return name;
    }

    public CollectionModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public CollectionModel setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isSystem() {
        return system;
    }

    public CollectionModel setSystem(boolean system) {
        this.system = system;
        return this;
    }

    public Schema getSchema() {
        return schema;
    }

    public CollectionModel setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public CollectionModel setIndexes(List<String> indexes) {
        this.indexes = indexes;
        return this;
    }

    public String getListRule() {
        return listRule;
    }

    public CollectionModel setListRule(String listRule) {
        this.listRule = listRule;
        return this;
    }

    public String getViewRule() {
        return viewRule;
    }

    public CollectionModel setViewRule(String viewRule) {
        this.viewRule = viewRule;
        return this;
    }

    public String getCreateRule() {
        return createRule;
    }

    public CollectionModel setCreateRule(String createRule) {
        this.createRule = createRule;
        return this;
    }

    public String getUpdateRule() {
        return updateRule;
    }

    public CollectionModel setUpdateRule(String updateRule) {
        this.updateRule = updateRule;
        return this;
    }

    public String getDeleteRule() {
        return deleteRule;
    }

    public CollectionModel setDeleteRule(String deleteRule) {
        this.deleteRule = deleteRule;
        return this;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public CollectionModel setOptions(Map<String, Object> options) {
        this.options = options;
        return this;
    }

    @Override
    public String toString() {
        return "CollectionModel{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
