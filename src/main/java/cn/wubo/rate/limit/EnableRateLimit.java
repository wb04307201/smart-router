package cn.wubo.rate.limit;

import cn.wubo.rate.limit.config.RateLimitConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({RateLimitConfig.class})
public @interface EnableRateLimit {
}
