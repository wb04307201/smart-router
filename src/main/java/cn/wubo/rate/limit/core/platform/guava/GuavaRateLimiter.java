package cn.wubo.rate.limit.core.platform.guava;

import cn.wubo.rate.limit.core.platform.IRateLimit;
import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuavaRateLimiter implements IRateLimit {

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * 尝试获取指定数量的许可，如果在指定时间内无法获取则返回false。
     *
     * @param key  限流的键标识符
     * @param count 需要获取的许可数量
     * @param time  获取许可的超时时间，单位与创建限流器时使用的单位相同
     * @return 如果成功获取到许可则返回true，否则返回false
     */
    @Override
    public boolean tryAcquire(String key, int count, int time) {
        // 获取或创建一个RateLimiter实例，每秒允许的请求数为count/time
        RateLimiter limiter = limiters.computeIfAbsent(key, k -> RateLimiter.create((double) count / time));
        return limiter.tryAcquire();
    }
}
