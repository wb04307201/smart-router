package cn.wubo.smart.router.utils;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void testValidStringAttributeWithMissingKey() {
        Map<String, Object> attributes = new HashMap<>();
        assertTrue(ValidationUtils.validStringAttribute(attributes, "key"));
    }

    @Test
    void testValidStringAttributeWithEmptyString() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "");
        assertTrue(ValidationUtils.validStringAttribute(attributes, "key"));
    }

    @Test
    void testValidStringAttributeWithWhitespaceString() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "   ");
        assertTrue(ValidationUtils.validStringAttribute(attributes, "key"));
    }

    @Test
    void testValidStringAttributeWithValidString() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "valid-value");
        assertFalse(ValidationUtils.validStringAttribute(attributes, "key"));
    }

    @Test
    void testValidListStringAttributeWithMissingKey() {
        Map<String, Object> attributes = new HashMap<>();
        assertTrue(ValidationUtils.validListStringAttribute(attributes, "key"));
    }

    @Test
    void testValidListStringAttributeWithEmptyList() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", List.of());
        assertTrue(ValidationUtils.validListStringAttribute(attributes, "key"));
    }

    @Test
    void testValidListStringAttributeWithInvalidItems() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", List.of("valid", "", "also-valid"));
        assertTrue(ValidationUtils.validListStringAttribute(attributes, "key"));
    }

    @Test
    void testValidListStringAttributeWithValidItems() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", List.of("valid", "also-valid", "another-valid"));
        assertFalse(ValidationUtils.validListStringAttribute(attributes, "key"));
    }

    @Test
    void testValidIntegerTypeAndRangeAttribute() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", 5);

        // 测试值在有效范围内
        assertFalse(ValidationUtils.validIntegerTypeAndRangeAttribute(attributes, "key", i -> i < 1 || i > 10));

        // 测试值超出范围
        assertTrue(ValidationUtils.validIntegerTypeAndRangeAttribute(attributes, "key", i -> i < 1 || i > 3));
    }
}
