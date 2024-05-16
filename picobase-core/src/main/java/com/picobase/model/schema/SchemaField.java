package com.picobase.model.schema;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.json.PbJsonTemplate;
import com.picobase.model.schema.fieldoptions.*;
import com.picobase.persistence.resolver.ListUtil;
import com.picobase.util.StringEscapeUtils;
import com.picobase.util.TypeSafe;

import static com.picobase.util.PbConstants.FIELD_VALUE_MODIFIER_ADD;
import static com.picobase.util.PbConstants.FIELD_VALUE_MODIFIER_SUBTRACT;
import static com.picobase.util.PbConstants.FieldType.Number;
import static com.picobase.util.PbConstants.FieldType.*;


public class SchemaField {

    private boolean system;

    private String id;

    private String name;

    private String type;

    private boolean required;

    private boolean presentable;

    private Object options;


    public SchemaField() {

    }

    public SchemaField(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public SchemaField(String name, String type, Object options) {
        this.name = name;
        this.type = type;
        this.options = options;
    }

    /**
     * prepareValue returns normalized and properly formatted field value.
     */
    public Object prepareValue(Object value) {
        // init field options (if not already)
        this.initOptions();

        return switch (this.type) {
            case Text, Email, Url, Editor -> TypeSafe.anyToString(value, "");
            case Json -> {
                var val = value;
                if (val instanceof String str) {
                    // in order to support seamlessly both json and multipart/form-data requests,
                    // the following normalization rules are applied for plain string values:
                    // - "true" is converted to the json `true`
                    // - "false" is converted to the json `false`
                    // - "null" is converted to the json `null`
                    // - "[1,2,3]" is converted to the json `[1,2,3]`
                    // - "{\"a\":1,\"b\":2}" is converted to the json `{"a":1,"b":2}`
                    // - numeric strings are converted to json number
                    // - double quoted strings are left as they are (aka. without normalizations)
                    // - any other string (empty string too) is double quoted

                    if (StrUtil.isEmpty(str)) {
                        val = "\"\"";
                    } else if (val.equals("null") || val.equals("true") || val.equals("false")) {
                        val = str;
                    } else if (
                            ((str.charAt(0) >= '0' && str.charAt(0) <= '9')
                                    || str.charAt(0) == '-'
                                    || str.charAt(0) == '"'
                                    || str.charAt(0) == '['
                                    || str.charAt(0) == '{') //TODO 这里没有判定是否为合法json字符串
                    ) {
                        val = str;
                    } else {
                        // throw new RuntimeException("not implemented"); // TODO  需要验证
                        val = StringEscapeUtils.escapeJson(str);
                    }
                }
                yield TypeSafe.anyToString(val, ""); // yield val;
            }
            case Number -> TypeSafe.anyToDouble(value);
            case Bool -> TypeSafe.anyToBool(value);
            case Date -> {
                var d = TypeSafe.anyToLocalDateTime(value);
                yield d == null ? "" : d;
            }
            case Select -> {
                var val = ListUtil.toUniqueStringList(value);

                var options = (SelectOptions) this.options;
                if (!options.isMultiple()) {
                    if (!val.isEmpty()) {
                        yield val.get(val.size() - 1); //TODO why 取最后一个element？
                    }
                    yield "";
                }
                yield val;
            }
            case File -> {
                var val = ListUtil.toUniqueStringList(value);

                var options = (FileOptions) this.options;
                if (!options.isMultiple()) {
                    if (!val.isEmpty()) {
                        yield val.get(val.size() - 1);
                    }
                    yield "";
                }
                yield val;
            }
            case Relation -> {
                var ids = ListUtil.toUniqueStringList(value);

                var options = (RelationOptions) this.options;
                if (!options.isMultiple()) {
                    if (!ids.isEmpty()) {
                        yield ids.get(ids.size() - 1); //TODO why 取最后一个element？
                    }
                    yield "";
                }
                yield ids;
            }
            default -> value == null ? "" : value;
        };
    }

    /**
     * initializes the current field options based on its type
     */
    public Error initOptions() {
        if (options instanceof FieldOptions) {
            return null; //已经初始化
        }
        Class<? extends FieldOptions> optionsClass = switch (this.type) {
            case Text -> TextOptions.class;
            case Number -> NumberOptions.class;
            case Bool -> BoolOptions.class;
            case Email -> EmailOptions.class;
            case Url -> UrlOptions.class;
            case Editor -> EditorOptions.class;
            case Date -> DateOptions.class;
            case Select -> SelectOptions.class;
            case Json -> JsonOptions.class;
            case File -> FileOptions.class;
            case Relation -> RelationOptions.class;
            default -> null;
        };
        if (optionsClass == null) {
            return new Error("Missing or unknown field field type. type: " + this.type);
        }
        if (options == null) {
            try {
                this.options = optionsClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return new Error(e.getMessage());
            }
        } else {
            // options is HashMap
            PbJsonTemplate pbJsonTemplate = PbManager.getPbJsonTemplate();
            this.options = pbJsonTemplate.parseJsonToObject(pbJsonTemplate.toJsonString(options), optionsClass);
        }

        return null;
    }


    public String colDefinition() {
        switch (type) {
            case Number:
                return "NUMERIC DEFAULT 0";
            case Bool:
                return "BOOLEAN DEFAULT FALSE";
            case Json:
                return "JSON DEFAULT NULL";
            case Text:
                return "VARCHAR(255) DEFAULT ''";
            default:
                this.initOptions();
                if (options instanceof MultiValuer && ((MultiValuer) options).isMultiple()) {
                    return "JSON DEFAULT NULL";
                }

                return "TEXT DEFAULT NULL";
        }
    }

    /**
     * PrepareValueWithModifier returns normalized and properly formatted field value
     * by "merging" baseValue with the modifierValue based on the specified modifier (+ or -).
     */
    public Object prepareValueWithModifier(Object baseValue, String modifier, Object modifierValue) {
        var resolvedValue = baseValue;
        switch (type) {
            case Number -> {
                switch (modifier) {
                    case FIELD_VALUE_MODIFIER_ADD ->
                            resolvedValue = TypeSafe.anyToDouble(baseValue) + TypeSafe.anyToDouble(modifierValue);
                    case FIELD_VALUE_MODIFIER_SUBTRACT ->
                            resolvedValue = TypeSafe.anyToDouble(baseValue) - TypeSafe.anyToDouble(modifierValue);
                }
            }
            case Select, Relation -> {
                switch (modifier) {
                    case FIELD_VALUE_MODIFIER_ADD ->
                            resolvedValue = CollUtil.addAllIfNotContains(ListUtil.toUniqueStringList(baseValue), ListUtil.toUniqueStringList(modifierValue));
                    case FIELD_VALUE_MODIFIER_SUBTRACT ->
                            resolvedValue = ListUtil.subtractList(ListUtil.toUniqueStringList(baseValue), ListUtil.toUniqueStringList(modifierValue));
                }
            }
            case File -> {
                if (modifier == FIELD_VALUE_MODIFIER_SUBTRACT) {
                    resolvedValue = ListUtil.subtractList(ListUtil.toUniqueStringList(baseValue), ListUtil.toUniqueStringList(modifierValue));
                }
            }
        }
        return prepareValue(resolvedValue);
    }


    public boolean isSystem() {
        return system;
    }

    public SchemaField setSystem(boolean system) {
        this.system = system;
        return this;
    }

    public String getId() {
        return id;
    }

    public SchemaField setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SchemaField setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public SchemaField setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public SchemaField setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public boolean isPresentable() {
        return presentable;
    }

    public SchemaField setPresentable(boolean presentable) {
        this.presentable = presentable;
        return this;
    }

    public Object getOptions() {
        return options;
    }

    public SchemaField setOptions(Object options) {
        this.options = options;
        return this;
    }


}

