package com.picobase.persistence.resolver;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import com.picobase.PbManager;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ListUtil {
    private static final int CACHE_LIMIT = 5000;
    private static final LRUCache<String, Pattern> cachedPatterns = new LRUCache<>(CACHE_LIMIT);


    /**
     * ToUniqueStringSlice casts `value` to a slice of non-zero unique strings.
     */
    public static List<String> toUniqueStringList(Object value) {
        List<String> result;
        if (value == null) {
            result = new ArrayList<>();
        } else if (value instanceof List) {
            result = (List<String>) value;
        } else if (ArrayUtil.isArray(value)) {
            Object[] objectArray;
            Class<?> componentType = value.getClass().getComponentType();
            if (componentType.isPrimitive()) { //原始类型数组
                int length = Array.getLength(value);
                objectArray = new Object[length];
                for (int i = 0; i < length; i++) {
                    objectArray[i] = Array.get(value, i); // 将原始类型数组中的元素转换为对应的包装类型
                }
            } else { //包装类型
                objectArray = (Object[]) value;
            }
            result = Arrays.asList(objectArray).stream().map(String::valueOf).collect(Collectors.toList());

        } else if (value instanceof String) {
            String val = (String) value;
            // check if it is a json encoded array of strings
            if (val.indexOf("[") != -1) {
                try {
                    List l = PbManager.getPbJsonTemplate().parseJsonToObject(val, List.class);
                    result = Convert.toList(String.class, l);
                } catch (Exception e) {
                    result = new ArrayList<>();
                    result.add(val);

                }
            } else {
                result = new ArrayList<>();
                result.add(val);
            }
        } else {
            throw new RuntimeException("Unsupported type: " + value.getClass());
        }
        return result.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

    }


    public static boolean existInListWithRegex(String str, List<String> list) {
        for (String field : list) {
            boolean isRegex = field.startsWith("^") && field.endsWith("$");

            if (!isRegex) {
                // Check for direct match
                if (str.equals(field)) {
                    return true;
                }
                continue;
            }

            // Check for regex match
            Pattern pattern = cachedPatterns.get(field);
            if (pattern == null) {
                try {
                    pattern = Pattern.compile(field);
                    // Cache the pattern to avoid compiling it every time
                    cachedPatterns.put(field, pattern);
                } catch (Exception e) {
                    throw new RuntimeException("Pattern.compile exception");
                }
            }

            if (pattern.matcher(str).matches()) {
                return true;
            }
        }

        return false;
    }

    public static boolean existInArray(String type, String[] strings) {
        return Arrays.stream(strings)
                .anyMatch(s -> s.equals(type));
    }

    public static boolean existInSlice(String item, List<String> list) {
        return list.stream()
                .anyMatch(s -> s.equals(item));
    }

    /**
     * Subtract List returns a new List with only the "base" elements that don't exist in "subtract"
     *
     * @param base
     * @param subtract
     * @return
     */
    public static <T> List<T> subtractList(List<T> base, List<T> subtract) {
        return base.stream().filter(b -> !subtract.contains(b)).collect(Collectors.toList());
    }
}


class LRUCache<K, V> {

    private final int maxSize;
    private final Map<K, V> cache;
    private final LinkedList<K> keys;

    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new HashMap<>();
        this.keys = new LinkedList<>();
    }

    public V get(K key) {
        V value = cache.get(key);
        if (value != null) {
            // Move the key to the front of the list
            keys.remove(key);
            keys.addFirst(key);
        }
        return value;
    }

    public void put(K key, V value) {
        if (cache.containsKey(key)) {
            // Update the value and move the key to the front of the list
            keys.remove(key);
        } else if (cache.size() == maxSize) {
            // Remove the least recently used key
            K lruKey = keys.removeLast();
            cache.remove(lruKey);
        }

        // Add the new key and value to the cache and list
        keys.addFirst(key);
        cache.put(key, value);
    }

    public boolean putIfLessThanLimit(K key, V value, int limit) {
        if (cache.size() < limit) {
            put(key, value);
            return true;
        }
        return false;
    }
}
