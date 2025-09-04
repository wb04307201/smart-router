package cn.wubo.rate.limiter.factory;

import cn.wubo.rate.limiter.RateLimiterProperties;
import cn.wubo.rate.limiter.bucket.IRateLimiter;
import cn.wubo.rate.limiter.bucket.RedissonRateLimiter;
import cn.wubo.rate.limiter.utils.ValidationUtils;
import jakarta.validation.Validator;
import org.redisson.Redisson;
import org.redisson.config.Config;

import java.util.Map;

public class RedisFactory implements IFactory {
    @Override
    public Boolean supports(String rateLimitingType) {
        return "redis".equals(rateLimitingType) ||
                "redis-cluster".equals(rateLimitingType) ||
                "redis-sentinel".equals(rateLimitingType);
    }

    @Override
    public IRateLimiter create(RateLimiterProperties properties, Validator validator) {
        Map<String, Object> attributes = properties.getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("Attributes map is null");
        }

        Config config = new Config();

        String type = properties.getRateLimitingType();
        if ("redis".equals(type)) {
            String address = ValidationUtils.getRequiredStringAttribute(attributes, "address", "Redis address is null or empty");
            String password = ValidationUtils.getRequiredStringAttribute(attributes, "password", "Redis password is null or empty");

            attributes.putIfAbsent("database", 0);
            Integer database = (Integer) attributes.get("database");

            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(password)
                    .setDatabase(database);

        } else if ("redis-cluster".equals(type)) {
            String[] nodes = ValidationUtils.getRequiredStringArrayAttribute(attributes, "nodes", "Redis cluster nodes is null or empty");
            String password = ValidationUtils.getRequiredStringAttribute(attributes, "password", "Redis cluster password is null or empty");

            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setPassword(password);

        } else if ("redis-sentinel".equals(type)) {
            String[] nodes = ValidationUtils.getRequiredStringArrayAttribute(attributes, "nodes", "Redis sentinel nodes is null or empty");
            String password = ValidationUtils.getRequiredStringAttribute(attributes, "password", "Redis sentinel password is null or empty");
            String masterName = ValidationUtils.getRequiredStringAttribute(attributes, "masterName", "Redis sentinel masterName is null or empty");

            attributes.putIfAbsent("database", 0);
            Integer database = (Integer) attributes.get("database");

            config.useSentinelServers()
                    .addSentinelAddress(nodes)
                    .setPassword(password)
                    .setDatabase(database)
                    .setMasterName(masterName);

        } else {
            throw new IllegalArgumentException("Unsupported rate limiting type: " + type);
        }

        return new RedissonRateLimiter(Redisson.create(config));
    }

}
