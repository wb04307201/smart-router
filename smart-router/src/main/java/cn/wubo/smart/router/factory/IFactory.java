package cn.wubo.smart.router.factory;

import cn.wubo.smart.router.SmartRouterProperties;
import cn.wubo.smart.router.bucket.IRateLimiter;
import jakarta.validation.Validator;

public interface IFactory {

    Boolean supports(String rateLimitingType);

    IRateLimiter create(SmartRouterProperties.RateLimiter properties);
}
