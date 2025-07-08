package cn.wubo.rate.limit.config;

import cn.wubo.rate.limit.core.RateLimitAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class RateLimitConfig {

    @Bean
    public RateLimitAspect rateLimitAspect() {
        return new RateLimitAspect();
    }
}
