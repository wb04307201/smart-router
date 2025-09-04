package cn.wubo.rate.limiter.factory;

import cn.wubo.rate.limiter.RateLimiterProperties;
import cn.wubo.rate.limiter.bucket.IRateLimiter;
import jakarta.validation.Validator;

public interface IFactory {

    Boolean supports(String rateLimitingType);

    IRateLimiter create(RateLimiterProperties properties, Validator validator);
}
