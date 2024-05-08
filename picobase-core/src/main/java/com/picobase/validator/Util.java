package com.picobase.validator;

import java.lang.reflect.Array;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class Util {

    /**
     * 计算 String、 List、 Map、Array 的长度（length or size）， 如果对象为 null 则返回为 -1
     * @param value 对象 String、 List、 Map、Array
     * @return 对象 length or size
     */
    public static int lengthOrSizeOfValue(Object value) {
        if (value == null){
            return -1;
        } else if (value instanceof String s) {
            return s.length();
        } else if (value instanceof List l) {
            return l.size();
        } else if (value instanceof Map m) {
            return m.size();
        } else if (value != null && value.getClass().isArray()) {
            return Array.getLength(value);
        } else {
            throw new IllegalStateException("cannot get the length");
        }
    }

    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof Integer || value instanceof Float) {
            return ((Number) value).doubleValue() == 0;
        } else if (value instanceof Boolean) {
            return !(Boolean) value;
        } else if (value instanceof String) {
            return ((String) value).length() == 0;
        } else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        } else if (value instanceof Map) {
            return ((Map) value).isEmpty();
        } else if (value instanceof Iterable) {
            return !((Iterable) value).iterator().hasNext();
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).equals(LocalTime.MIDNIGHT);
        }

        return false;
    }

    public static Object indirect(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Map || value instanceof Iterable) {
            if (((Map<?, ?>) value).isEmpty()) {
                return null;
            }
        }
        return value;
    }


    public static String ensureString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value);
        }
        throw new IllegalStateException("must be either a string or byte slice");
    }

}
