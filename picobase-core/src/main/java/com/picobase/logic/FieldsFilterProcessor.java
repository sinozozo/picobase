package com.picobase.logic;

import cn.hutool.core.util.StrUtil;
import com.picobase.util.Tokenizer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FieldsFilterProcessor {


    public static void pickFields(Object data, String rawFields) {
        var parsedFields = parseFields(rawFields);

        pickParsedFields(data, parsedFields);
    }

    private static void pickParsedFields(Object data, Map<String, FieldModifier> parsedFields) {
        if (data instanceof List list) {
            list.forEach(item -> {
                if (item instanceof Map m) {
                    pickMapFields(m, parsedFields);
                } else {
                    // for now ignore non-map values
                }
            });
        } else if (data instanceof Map map) {
            pickMapFields(map, parsedFields);
        }
    }


    /*
     * 根据 parsedFields 更新 data 结构
     * @fields 被允许的字段，FieldModifier 可能为null;
     */
    private static void pickMapFields(Map<String, Object> data, Map<String, FieldModifier> fields) {
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
        for (String k : new HashSet<>(data.keySet())) {
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
            for (String f : new HashSet<String>(matchingFields.keySet())) {
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


    private static Map<String, FieldModifier> parseFields(String rawFields) {
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

    private static FieldModifier initModifer(String rawModifier) {
        var t = Tokenizer.newFromString(rawModifier);
        t.setSeparators('(', ')', ',', ' ');
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
