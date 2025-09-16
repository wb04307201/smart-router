package cn.wubo.smart.router;

import cn.wubo.smart.router.annotation.RateLimiterValidator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "smart-router")
public class SmartRouterProperties {
    private RateLimiter rateLimiter = new RateLimiter();
    private List<RateLimitRule> rateLimitRules = new ArrayList<>();
    private List<ProxyRule> proxyRules = new ArrayList<>();

    @Data
    @RateLimiterValidator
    public static class RateLimiter{
        // redis单点 redis, redis集群 redis-cluster, redis哨兵 redis-sentinel, standalone
        private String rateLimitingType = "standalone";
        private Map<String, Object> attributes = new HashMap<>();
    }

    @Data
    public static class RateLimitRule {
        private String endpoint;
        private long capacity;
        private long period;
        private TimeUnit unit = TimeUnit.SECONDS;
    }

    @Data
    public static class ProxyRule {
        private String endpoint;
        private List<Proxy> proxies = new ArrayList<>();

        @Data
        public static class Proxy {
            private String targetEndpoint;
            private long weight;
        }
    }
}
