package com.picobase.console.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.picobase.context.PbHolder;
import com.picobase.context.model.PbRequest;
import com.picobase.model.RecordModel;
import com.picobase.util.Tokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.picobase.util.PbConstants.QueryParam.FIELDS;

/**
 * 针对 fileds filter 封装处理
 *
 * @param <T>
 */
public abstract class AbstractFieldsFilterProcessor<T> extends JsonSerializer<T> {


    public void serialize(T t, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Map<String, Object> exportedData = convertBeanToExportedMap(t);

        PbRequest request = PbHolder.getRequest();
        String fields = request.getParam(FIELDS);

        if (!StrUtil.isEmpty(fields)) {
            //根据fields 清洗待导出的Map
            pickFields(exportedData, fields);
        }

        //执行序列化
        gen.writeObject(exportedData);
    }

    /**
     * 子类实现 bean 转换为Map ， 最终会导出这个map 作为json响应
     *
     * @param t
     * @return
     */
    public abstract Map<String, Object> convertBeanToExportedMap(T t);

    protected void pickFields(Map<String, Object> data, String rawFields) {
        var parsedFields = parseFields(rawFields);

        pickParsedFields(data, parsedFields);
    }

    private void pickParsedFields(Object data, Map<String, FieldModifier> parsedFields) {
        if (data instanceof List list) {
            list.forEach(item -> {
                if (item instanceof Map m) {
                    pickMapFields(m, parsedFields);
                } else if (item instanceof RecordModel record) {
                    pickParsedFields(convertBeanToExportedMap((T) record), parsedFields);
                } else {
                    throw new IllegalArgumentException("不应存在的类型");
                }

                //pickMapFields(item, parsedFields);
            });
        } else if (data instanceof Map map) {
            pickMapFields(map, parsedFields);
        } else if (data instanceof RecordModel record) { //当 record 中包含 expand 数据时
            pickParsedFields(convertBeanToExportedMap((T) record), parsedFields);
        }

    }

    /*
     * 根据 parsedFields 更新 data 结构
     * @fields 被允许的字段，FieldModifier 可能为null;
     */
    private void pickMapFields(Map<String, Object> data, Map<String, FieldModifier> fields) {
        if (fields.isEmpty()) {
            return; // nothing to pick
        }
        if (fields.containsKey("*")) {
            var m = fields.get("*");
            // append all missing root level data keys
            data.keySet().forEach(k -> {
                boolean exsists = false;
                Set<String> fs = fields.keySet();
                for (String f : fs) {
                    if ((f + ".").startsWith(k + ".")) {
                        exsists = true;
                        break;
                    }
                }

                if (!exsists) {
                    fields.put(k, m);
                }

            });
        }


        DataLoop:
        for (String k : data.keySet()) {
            var matchingFields = new ConcurrentHashMap<String, FieldModifier>(fields.size());
            fields.entrySet().forEach(e -> {
                var f = e.getKey();
                if ((f + ".").startsWith(k + ".")) {
                    matchingFields.put(f, e.getValue());
                }
            });

            if (matchingFields.isEmpty()) {
                data.remove(k);
                continue DataLoop;
            }
            // remove the current key from the matching fields path
            for (String f : matchingFields.keySet()) {
                var m = matchingFields.get(f);
                var remains = StrUtil.removeSuffix(StrUtil.removePrefix(f + ".", k + "."), ".");
                // final key
                if (StrUtil.isEmpty(remains)) {
                    data.put(k, m.modify(data.get(k)));
                    continue DataLoop;
                }

                // cleanup the old field key and continue with the rest of the field path
                matchingFields.remove(f);
                matchingFields.put(remains, m);
            }

            pickParsedFields(data.get(k), matchingFields);
        }
    }


    private Map<String, FieldModifier> parseFields(String rawFields) {
        var t = Tokenizer.newFromString(rawFields);
        var fields = t.scanAll();
        var result = new HashMap<String, FieldModifier>(fields.size());
        fields.forEach(field -> {
            var parts = field.trim().split(":", 2);
            if (parts.length > 1) {
                var modifier = initModifer(parts[1]);
                result.put(parts[0], modifier);
            } else {
                result.put(parts[0], FieldModifier.Nil_Modifier);
            }
        });
        return result;
    }

    private FieldModifier initModifer(String rawModifier) {
        var t = Tokenizer.newFromString(rawModifier);
        t.setSeparators(new char[]{'(', ')', ',', ' '});
        t.setIgnoreParenthesis(true);
        var parts = t.scanAll();
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("invalid or empty modifier expression " + rawModifier);
        }
        var name = parts.get(0);
        var args = parts.subList(1, parts.size());
        switch (name) {
            case "excerpt":
                return FieldModifier.newExcerptModifier(args);
            default:
                throw new IllegalArgumentException("invalid modifier name " + name);
        }
    }


}
