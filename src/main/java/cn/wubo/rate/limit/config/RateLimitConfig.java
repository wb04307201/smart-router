package cn.wubo.rate.limit.config;

import cn.wubo.rate.limit.core.RateLimitAspect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableConfigurationProperties({RateLimitProperties.class})
@EnableAspectJAutoProxy
public class RateLimitConfig {

    @Bean
    public RateLimitAspect rateLimitAspect(RateLimitProperties rateLimitProperties) {
        return new RateLimitAspect(rateLimitProperties);
    }
}
