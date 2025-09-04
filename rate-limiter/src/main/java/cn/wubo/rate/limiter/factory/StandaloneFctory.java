package cn.wubo.rate.limiter.factory;

import cn.wubo.rate.limiter.RateLimiterProperties;
import cn.wubo.rate.limiter.bucket.GuavaRateLimiter;
import cn.wubo.rate.limiter.bucket.IRateLimiter;
import jakarta.validation.Validator;

public class StandaloneFctory implements IFactory{
    @Override
    public Boolean supports(String rateLimitingType) {
        return "standalone".equals(rateLimitingType);
    }

    @Override
    public IRateLimiter create(RateLimiterProperties properties, Validator validator) {
        return new GuavaRateLimiter();
    }
}
