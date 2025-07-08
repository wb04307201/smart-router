package cn.wubo.rate.limit.core;

import cn.wubo.rate.limit.annotation.RateLimit;
import cn.wubo.rate.limit.config.RateLimitProperties;
import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class RateLimitAspect {

    private RateLimitProperties rateLimitProperties;

    public RateLimitAspect(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
    }

    private static final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * 检查方法调用是否超过限流阈值
     *
     * @param jp        切面连接点，包含被拦截的方法信息
     * @param rateLimit 限流注解实例，包含限流配置参数
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint jp, RateLimit rateLimit) throws Throwable {
        String key = jp.getSignature().toLongString();
        // 使用Guava的RateLimiter实现令牌桶限流算法
        // 如果已存在对应key的限流器则直接使用，否则根据注解配置创建新的限流器
        RateLimiter limiter = limiters.computeIfAbsent(key,
                k -> RateLimiter.create(rateLimit.count() / rateLimit.time()));

        // 尝试获取令牌：支持两种模式
        // 1. 带超时等待的获取（指定超时时间）
        // 2. 不带超时等待的立即获取
        Boolean acquired = rateLimit.timeout() > 0 ? limiter.tryAcquire(rateLimit.timeout(), rateLimit.timeUnit()) : limiter.tryAcquire();

        // 如果未能获取到令牌，抛出运行时异常并携带自定义提示信息
        if (!acquired) {
            throw new RuntimeException(rateLimit.message());
        }

        return jp.proceed();
    }
}