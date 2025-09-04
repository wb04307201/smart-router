package cn.wubo.rate.limiter.utils;

import java.util.Map;


public class ValidationUtils {

    private ValidationUtils() {
    }


    public static String getRequiredStringAttribute(Map<String, Object> attributes, String key, String errorMessage) {
        Object value = attributes.get(key);
        if (!(value instanceof String str) || str.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return str;
    }

    public static String[] getRequiredStringArrayAttribute(Map<String, Object> attributes, String key, String errorMessage) {
        Object value = attributes.get(key);
        if (!(value instanceof String[] strs) || strs.length == 0) {
            throw new IllegalArgumentException(errorMessage);
        }
        return strs;
    }

}
