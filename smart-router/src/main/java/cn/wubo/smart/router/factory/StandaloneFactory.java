package cn.wubo.smart.router.factory;

import cn.wubo.smart.router.SmartRouterProperties;
import cn.wubo.smart.router.bucket.GuavaRateLimiter;
import cn.wubo.smart.router.bucket.IRateLimiter;
import jakarta.validation.Valid;

public class StandaloneFactory implements IFactory {
    @Override
    public Boolean supports(String rateLimitingType) {
        return "standalone".equals(rateLimitingType);
    }

    @Override
    public IRateLimiter create(@Valid SmartRouterProperties.RateLimiter properties) {
        return new GuavaRateLimiter();
    }
}
