package cn.wubo.rate.limiter;

import cn.wubo.rate.limiter.annotation.RateLimiterPropertiesValidator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@RateLimiterPropertiesValidator
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    // // redis单点 redis, redis集群 redis-cluster, redis哨兵 redis-sentinel, standalone
    private String rateLimitingType = "standalone";

    private Map<String, Object> attributes = new HashMap<>();

    private List<RateLimitRule> rules = new ArrayList<>();

    @Data
    public static class RateLimitRule {
        private String endpoint;
        private long capacity;
        private long period;
        private TimeUnit unit = TimeUnit.SECONDS;
    }
}
