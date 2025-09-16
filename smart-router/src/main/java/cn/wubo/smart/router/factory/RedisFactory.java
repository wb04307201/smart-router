package cn.wubo.smart.router.factory;

import cn.wubo.smart.router.SmartRouterProperties;
import cn.wubo.smart.router.bucket.IRateLimiter;
import cn.wubo.smart.router.bucket.RedissonRateLimiter;
import jakarta.validation.Valid;
import org.redisson.Redisson;
import org.redisson.config.Config;

import java.util.List;
import java.util.Map;

public class RedisFactory implements IFactory {
    @Override
    public Boolean supports(String rateLimitingType) {
        return "redis".equals(rateLimitingType) ||
                "redis-cluster".equals(rateLimitingType) ||
                "redis-sentinel".equals(rateLimitingType);
    }

    @Override
    public IRateLimiter create(@Valid SmartRouterProperties.RateLimiter properties) {
        Map<String, Object> attributes = properties.getAttributes();
        Config config = new Config();

        String type = properties.getRateLimitingType();
        if ("redis".equals(type)) {
            String address = (String) attributes.get("address");
            String password = (String) attributes.get("password");

            Integer database = (Integer) attributes.getOrDefault("database",0);

            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(password)
                    .setDatabase(database);

        } else if ("redis-cluster".equals(type)) {
            List<String> nodes = (List<String>)attributes.get("nodes");
            String password = (String) attributes.get("password");

            config.useClusterServers()
                    .addNodeAddress(nodes.toArray(new String[0]))
                    .setPassword(password);

        } else if ("redis-sentinel".equals(type)) {
            List<String> nodes = (List<String>)attributes.get("nodes");
            String password = (String) attributes.get("password");
            String masterName = (String) attributes.get("masterName");

            Integer database = (Integer) attributes.getOrDefault("database",0);

            config.useSentinelServers()
                    .addSentinelAddress(nodes.toArray(new String[0]))
                    .setPassword(password)
                    .setDatabase(database)
                    .setMasterName(masterName);

        } else {
            throw new IllegalArgumentException("Unsupported rate limiting type: " + type);
        }

        return new RedissonRateLimiter(Redisson.create(config));
    }

}
