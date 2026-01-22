package cn.wubo.smart.router.utils;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@UtilityClass
public class ValidationUtils {

    public boolean validStringAttribute(Map<String, Object> attributes, String key) {
        return !attributes.containsKey(key) || !(attributes.get(key) instanceof String str) || str.trim().isEmpty();
    }

    public boolean validListStringAttribute(Map<String, Object> attributes, String key) {
        if (!attributes.containsKey(key) || !(attributes.get(key) instanceof List<?> list) || list.isEmpty())
            return true;

        for (Object item : (List<?>) attributes.get(key)) {
            if (!(item instanceof String str) || str.trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public boolean validIntegerTypeAndRangeAttribute(Map<String, Object> attributes, String key, Predicate<Integer> predicate) {
        return attributes.containsKey(key) && (!(attributes.get(key) instanceof Integer integer) || predicate.test(integer));
    }
}
