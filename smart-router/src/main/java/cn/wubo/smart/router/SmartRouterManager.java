package cn.wubo.smart.router;

import cn.wubo.smart.router.bucket.IRateLimiter;
import cn.wubo.smart.router.dto.Rule;
import cn.wubo.smart.router.expression.SpelParamModifier;
import cn.wubo.smart.router.http.MutableHttpServletRequestWrapper;
import cn.wubo.smart.router.storage.IStorage;
import cn.wubo.smart.router.storage.RouterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SmartRouterManager {

    @Getter
    private List<SmartRouterProperties.RateLimitRule> rateLimitRules;
    @Getter
    private List<SmartRouterProperties.ProxyRule> proxyRules;
    private final IRateLimiter bucket;
    private final IStorage storage;
    private final PathMatcher MATCHER;
    private final Random random;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public SmartRouterManager(SmartRouterProperties properties, IRateLimiter bucket, IStorage storage) {
        this.rateLimitRules = properties.getRateLimitRules();
        this.proxyRules = properties.getProxyRules();
        this.bucket = bucket;
        this.storage = storage;
        this.MATCHER = new AntPathMatcher();
        this.random = new Random();
    }

    public Boolean rule(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String endpoint = request.getRequestURI();

        RouterInfo.RouterInfoBuilder builder = RouterInfo.builder()
                .endpoint(endpoint);

        Boolean isContinue;
        isContinue = rateLimit(endpoint, request, response, builder);
        isContinue = isContinue && proxy(endpoint, request, response, builder);
        storage.add(builder.build());
        return isContinue;
    }

    /**
     * 对指定端点进行限流检查
     *
     * @param endpoint 要检查的端点路径
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param builder  路由信息构建器
     * @return 如果未超过限流限制返回true，否则返回false并设置相应的错误响应
     * @throws IOException 当发送错误响应时可能抛出IO异常
     */
    public Boolean rateLimit(String endpoint, HttpServletRequest request, HttpServletResponse response, RouterInfo.RouterInfoBuilder builder) throws IOException {
        // 检查是否存在限流规则配置
        if (!rateLimitRules.isEmpty()) {
            // 查找匹配当前端点的限流规则
            Optional<SmartRouterProperties.RateLimitRule> rateLimitRuleOptional = rateLimitRules
                    .stream()
                    .filter(item -> MATCHER.match(item.getEndpoint(), endpoint))
                    .findFirst();

            // 如果找到匹配的限流规则
            if (rateLimitRuleOptional.isPresent()) {
                builder.isRateLimit(true);
                SmartRouterProperties.RateLimitRule rateLimitRule = rateLimitRuleOptional.get();
                // 尝试获取令牌桶中的令牌，如果获取失败则表示超过限流限制
                if (!bucket.tryAcquire(endpoint, rateLimitRule.getCapacity(), rateLimitRule.getPeriod())) {
                    builder.isConsum(false);
                    response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 处理代理请求的方法
     * 根据配置的代理规则，将请求转发到目标端点
     *
     * @param endpoint 请求的目标端点路径
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param builder  路由信息构建器
     * @return 如果执行了代理转发则返回false，否则返回true
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    public Boolean proxy(String endpoint, HttpServletRequest request, HttpServletResponse response, RouterInfo.RouterInfoBuilder builder) throws IOException, ServletException {
        // 检查是否存在代理规则
        if (!proxyRules.isEmpty()) {
            // 查找匹配当前端点的代理规则
            Optional<SmartRouterProperties.ProxyRule> proxyRuleOptional = proxyRules
                    .stream()
                    .filter(item -> MATCHER.match(item.getEndpoint(), endpoint))
                    .findFirst();

            if (proxyRuleOptional.isPresent()) {
                builder.isProxy(true);

                SmartRouterProperties.ProxyRule proxyRule = proxyRuleOptional.get();
                // 计算所有代理权重的总和
                long totalWeight = proxyRule.getProxies().stream().mapToLong(SmartRouterProperties.ProxyRule.Proxy::getWeight).sum();

                // 生成随机权重点用于负载均衡
                long randomPoint = random.nextLong(totalWeight);
                long cumulativeWeight = 0;

                // 遍历代理列表，根据权重选择目标代理
                for (SmartRouterProperties.ProxyRule.Proxy proxy : proxyRule.getProxies()) {
                    cumulativeWeight += proxy.getWeight();
                    if (randomPoint < cumulativeWeight) {
                        String mapRule = proxy.getMapRule();
                        String bodyRule = proxy.getBodyRule();

                        if (StringUtils.hasText(mapRule) || StringUtils.hasText(bodyRule)) {
                            MutableHttpServletRequestWrapper mutableRequest = new MutableHttpServletRequestWrapper(request);

                            if (StringUtils.hasText(mapRule)) {
                                Map<String, String[]> modifiedMap = new HashMap<>(mutableRequest.getParameterMap());
                                SpelParamModifier.modifyParam(modifiedMap, mapRule);
                                modifiedMap.forEach(mutableRequest::setParameter);
                                builder.isMap(true).mapContent(OBJECT_MAPPER.writeValueAsString(modifiedMap));
                            }

                            if (StringUtils.hasText(bodyRule)) {
                                String originalBody = StreamUtils.copyToString(mutableRequest.getInputStream(), StandardCharsets.UTF_8);
                                Map<String, Object> bodyMap = OBJECT_MAPPER.readValue(originalBody, Map.class);
                                SpelParamModifier.modifyJsonBody(bodyMap, bodyRule);
                                String modifiedBody = OBJECT_MAPPER.writeValueAsString(bodyMap);
                                mutableRequest.setJsonBody(modifiedBody);
                                builder.isBody(true).bodyContent(modifiedBody);
                            }

                            request = mutableRequest;
                        }

                        builder.targetEndpoint(proxy.getTargetEndpoint());
                        request.getRequestDispatcher(proxy.getTargetEndpoint()).forward(request, response);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void updateRules(Rule rule) {
        this.bucket.clear();
        this.rateLimitRules = rule.getRateLimitRules();
        this.proxyRules = rule.getProxyRules();
        this.storage.reset();
    }

    public Rule getRules() {
        Rule rule = new Rule();
        rule.setRateLimitRules(rateLimitRules);
        rule.setProxyRules(proxyRules);
        return rule;
    }

}
