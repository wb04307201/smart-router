package cn.wubo.rate.limit.core;

import cn.wubo.rate.limit.annotation.RateLimit;
import cn.wubo.rate.limit.core.platform.IRateLimit;
import cn.wubo.rate.limit.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class RateLimitAspect {

    private final IRateLimit rateLimiter;

    public RateLimitAspect(IRateLimit rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * 环绕通知方法，在带有@RateLimit注解且属于RestController的方法执行时被调用
     *
     * @param jp        连接点对象，包含目标方法的详细信息
     * @param rateLimit 注解对象，包含限流配置参数
     * @return Object 目标方法的返回值
     * @throws Throwable 如果目标方法或限流检查过程中抛出异常
     */
    @Around("@annotation(rateLimit) && @within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint jp, RateLimit rateLimit) throws Throwable {
        String key = jp.getSignature().toLongString();
        // 使用限流器尝试获取许可，根据方法签名生成限流键值
        log.debug("正在调用方法 {} 每个窗口允许请求数 {} 窗口时长 {} 秒 尝试限获取限流器许可 ", key, rateLimit.count(), rateLimit.time());
        Boolean acquired = rateLimiter.tryAcquire(key, rateLimit.count(), rateLimit.time());
        log.debug("正在调用方法 {} 限流器尝试许可结果 {}", key, acquired);
        if (!acquired) {
            // 如果未获取到许可，抛出限流异常
            throw new RateLimitException(rateLimit.message());
        }

        return jp.proceed();
    }
}