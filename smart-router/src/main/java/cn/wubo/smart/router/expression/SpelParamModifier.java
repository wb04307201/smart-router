package cn.wubo.smart.router.expression;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;

@UtilityClass
public class SpelParamModifier {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void modifyParam(Map<String, String[]> params, String spelExpression) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("params", params);
        PARSER.parseExpression(spelExpression).getValue(context);
    }

    public void modifyJsonBody(Map<String, Object> params, String spelExpression) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("params", params);
        PARSER.parseExpression(spelExpression).getValue(context);
    }
}
