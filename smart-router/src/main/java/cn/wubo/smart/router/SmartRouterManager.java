package cn.wubo.smart.router;

import cn.wubo.smart.router.bucket.IRateLimiter;
import cn.wubo.smart.router.dto.Rule;
import cn.wubo.smart.router.storage.IStorage;
import cn.wubo.smart.router.storage.RouterInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.io.IOException;
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

    public Boolean rateLimit(String endpoint, HttpServletRequest request, HttpServletResponse response, RouterInfo.RouterInfoBuilder builder) throws IOException {
        if (!rateLimitRules.isEmpty()) {
            Optional<SmartRouterProperties.RateLimitRule> rateLimitRuleOptional = rateLimitRules
                    .stream()
                    .filter(item -> MATCHER.match(item.getEndpoint(), endpoint))
                    .findFirst();

            if (rateLimitRuleOptional.isPresent()) {
                builder.isRateLimit(true);
                SmartRouterProperties.RateLimitRule rateLimitRule = rateLimitRuleOptional.get();
                if (!bucket.tryAcquire(endpoint, rateLimitRule.getCapacity(), rateLimitRule.getPeriod())) {
                    builder.isConsum(false);
                    response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
                    return false;
                }
            }
        }

        return true;
    }

    public Boolean proxy(String endpoint, HttpServletRequest request, HttpServletResponse response, RouterInfo.RouterInfoBuilder builder) throws IOException, ServletException {
        if (!proxyRules.isEmpty()) {
            Optional<SmartRouterProperties.ProxyRule> proxyRuleOptional = proxyRules
                    .stream()
                    .filter(item -> MATCHER.match(item.getEndpoint(), endpoint))
                    .findFirst();

            if (proxyRuleOptional.isPresent()) {
                builder.isProxy(true);
                SmartRouterProperties.ProxyRule proxyRule = proxyRuleOptional.get();
                long totalWeight = proxyRule.getProxies().stream().mapToLong(SmartRouterProperties.ProxyRule.Proxy::getWeight).sum();

                long randomPoint = random.nextLong(totalWeight);
                long cumulativeWeight = 0;

                for (SmartRouterProperties.ProxyRule.Proxy proxy : proxyRule.getProxies()) {
                    cumulativeWeight += proxy.getWeight();
                    if (randomPoint < cumulativeWeight) {
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
