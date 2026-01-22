package cn.wubo.smart.router.expression;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.SpelParseException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpelParamModifier单元测试类
 * Tests for SpEL parameter and JSON body modification functionality
 */
public class SpelParamModifierTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 测试modifyParam方法的基本功能
     * Test basic functionality of modifyParam method
     */
    @Test
    @DisplayName("测试基本参数修改功能")
    void testModifyParamBasic() {
        Map<String, String[]> params = new HashMap<>();
        params.put("name", new String[]{"oldValue"});

        // 修改参数值
        SpelParamModifier.modifyParam(params, "#params['name'][0] = 'newValue'");

        assertEquals("newValue", params.get("name")[0]);
    }

    /**
     * 测试modifyParam方法添加新参数
     * Test adding new parameters with modifyParam method
     */
    @Test
    @DisplayName("测试添加新参数")
    void testModifyParamAddNewParameter() {
        Map<String, String[]> params = new HashMap<>();
        params.put("existing", new String[]{"value"});

        // 添加新参数
        SpelParamModifier.modifyParam(params, "#params['newParam'] = new String[]{'newValue'}");

        assertTrue(params.containsKey("newParam"));
        assertEquals("newValue", params.get("newParam")[0]);
    }

    /**
     * 测试modifyParam方法处理复杂数据结构
     * Test handling complex data structures with modifyParam method
     */
    @Test
    @DisplayName("测试处理复杂参数结构")
    void testModifyParamComplexStructure() {
        Map<String, String[]> params = new HashMap<>();
        params.put("arrayParam", new String[]{"val1", "val2", "val3"});

        // 修改数组中的特定元素
        SpelParamModifier.modifyParam(params, "#params['arrayParam'][1] = 'modifiedVal'");

        assertArrayEquals(new String[]{"val1", "modifiedVal", "val3"}, params.get("arrayParam"));
    }

    /**
     * 测试modifyParam方法处理空参数
     * Test handling null parameters with modifyParam method
     */
    @Test
    @DisplayName("测试空参数处理")
    void testModifyParamWithNullParams() {
        assertThrows(Exception.class, () -> {
            SpelParamModifier.modifyParam(null, "#params['test'] = 'value'");
        });
    }

    /**
     * 测试modifyParam方法处理无效SpEL表达式
     * Test handling invalid SpEL expressions in modifyParam method
     */
    @Test
    @DisplayName("测试无效SpEL表达式处理")
    void testModifyParamInvalidExpression() {
        Map<String, String[]> params = new HashMap<>();
        params.put("test", new String[]{"value"});

        assertThrows(SpelParseException.class, () -> {
            SpelParamModifier.modifyParam(params, "invalid[expression[[");
        });
    }

    /**
     * 测试modifyJsonBody方法的基本功能
     * Test basic functionality of modifyJsonBody method
     */
    @Test
    @DisplayName("测试基本JSON修改功能")
    void testModifyJsonBodyBasic() throws JsonProcessingException {
        String jsonBody = "{\"name\":\"oldName\", \"age\":30}";
        String spelExpression = "#params['name'] = 'newName'";

        Map<String, Object> bodyMap = OBJECT_MAPPER.readValue(jsonBody, Map.class);
        SpelParamModifier.modifyJsonBody(bodyMap, spelExpression);
        String result = OBJECT_MAPPER.writeValueAsString(bodyMap);

        assertTrue(result.contains("\"name\":\"newName\""));
        assertTrue(result.contains("\"age\":30"));
    }

    /**
     * 测试modifyJsonBody方法修改嵌套对象
     * Test modifying nested objects with modifyJsonBody method
     */
    @Test
    @DisplayName("测试修改嵌套JSON对象")
    void testModifyJsonBodyNestedObject() throws JsonProcessingException {
        String jsonBody = "{\"user\":{\"name\":\"oldName\", \"details\":{\"age\":30}}}";
        String spelExpression = "#params['user']['details']['age'] = 25";

        Map<String, Object> bodyMap = OBJECT_MAPPER.readValue(jsonBody, Map.class);
        SpelParamModifier.modifyJsonBody(bodyMap, spelExpression);
        String result = OBJECT_MAPPER.writeValueAsString(bodyMap);

        assertTrue(result.contains("\"age\":25"));
        assertTrue(result.contains("\"name\":\"oldName\""));
    }

    /**
     * 测试modifyJsonBody方法修改数组元素
     * Test modifying array elements with modifyJsonBody method
     */
    @Test
    @DisplayName("测试修改JSON数组元素")
    void testModifyJsonBodyArrayElement() throws JsonProcessingException {
        String jsonBody = "{\"items\":[\"item1\", \"item2\", \"item3\"]}";
        String spelExpression = "#params['items'][1] = 'modifiedItem'";

        Map<String, Object> bodyMap = OBJECT_MAPPER.readValue(jsonBody, Map.class);
        SpelParamModifier.modifyJsonBody(bodyMap, spelExpression);
        String result = OBJECT_MAPPER.writeValueAsString(bodyMap);

        assertTrue(result.contains("\"items\":[\"item1\",\"modifiedItem\",\"item3\"]"));
    }

    /**
     * 测试modifyJsonBody方法添加新字段
     * Test adding new fields with modifyJsonBody method
     */
    @Test
    @DisplayName("测试添加新JSON字段")
    void testModifyJsonBodyAddField() throws JsonProcessingException {
        String jsonBody = "{\"name\":\"test\"}";
        String spelExpression = "#params['newField'] = 'newValue'";

        Map<String, Object> bodyMap = OBJECT_MAPPER.readValue(jsonBody, Map.class);
        SpelParamModifier.modifyJsonBody(bodyMap, spelExpression);
        String result = OBJECT_MAPPER.writeValueAsString(bodyMap);

        assertTrue(result.contains("\"name\":\"test\""));
        assertTrue(result.contains("\"newField\":\"newValue\""));
    }

    /**
     * 测试modifyJsonBody方法处理无效SpEL表达式
     * Test handling invalid SpEL expressions in modifyJsonBody method
     */
    @Test
    @DisplayName("测试JSON中无效SpEL表达式处理")
    void testModifyJsonBodyInvalidSpelExpression() throws JsonProcessingException {
        String jsonBody = "{\"name\":\"test\"}";
        String invalidSpelExpression = "#params..invalid[[expression";

        assertThrows(ParseException.class, () -> {
            Map<String, Object> bodyMap = OBJECT_MAPPER.readValue(jsonBody, Map.class);
            SpelParamModifier.modifyJsonBody(bodyMap, invalidSpelExpression);
        });
    }

    /**
     * 测试modifyJsonBody方法处理空JSON字符串
     * Test handling empty JSON string with modifyJsonBody method
     */
    @Test
    @DisplayName("测试空JSON字符串处理")
    void testModifyJsonBodyEmptyString() {
        assertThrows(JsonProcessingException.class, () -> {
            SpelParamModifier.modifyJsonBody(OBJECT_MAPPER.readValue("", Map.class),
                    "#params['test'] = 'value'");
        });
    }
}
