package cn.wubo.rate.limit.config;

import cn.wubo.rate.limit.core.RateLimitAspect;
import cn.wubo.rate.limit.core.RedissionRateLimiter;
import cn.wubo.rate.limit.core.platform.IRateLimit;
import cn.wubo.rate.limit.core.platform.guava.GuavaRateLimiter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({RateLimitProperties.class})
public class RateLimitConfig {

    /**
     * 创建并返回一个IRateLimit实例，根据配置的限流类型初始化不同的限流实现。
     * 
     * @param properties 限流配置属性，包含限流类型和Redis相关配置
     * @return 初始化后的IRateLimit实例
     */
    @Bean
    public IRateLimit rateLimiter(RateLimitProperties properties) {
        if("guava".equals(properties.getRateLimitType())){
            return new GuavaRateLimiter();
        }else {
            /**
             * 根据不同的限流类型初始化Redis配置
             * 支持的类型包括单机Redis、Redis集群和Redis哨兵模式
             */
            Config config = new Config();
            if ("redis".equals(properties.getRateLimitType())) {
                config.useSingleServer().setAddress(properties.getRedis().getAddress()).setPassword(properties.getRedis().getPassword()).setDatabase(properties.getRedis().getDatabase());
            } else if ("redis-cluster".equals(properties.getRateLimitType())) {
                config.useClusterServers().addNodeAddress(properties.getRedis().getNodes().toArray(new String[0])).setPassword(properties.getRedis().getPassword());
            } else if ("redis-sentinel".equals(properties.getRateLimitType())) {
                config.useSentinelServers().addSentinelAddress(properties.getRedis().getNodes().toArray(new String[0])).setPassword(properties.getRedis().getPassword()).setDatabase(properties.getRedis().getDatabase()).setMasterName(properties.getRedis().getMasterName());
            }

            return new RedissionRateLimiter(Redisson.create(config));
        }
    }

    @Bean
    public RateLimitAspect rateLimitAspect(IRateLimit rateLimit) {
        return new RateLimitAspect(rateLimit);
    }
}
