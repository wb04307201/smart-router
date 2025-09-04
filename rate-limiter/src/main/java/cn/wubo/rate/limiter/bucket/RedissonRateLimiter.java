package cn.wubo.rate.limiter.bucket;

import com.google.common.util.concurrent.RateLimiter;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedissonRateLimiter implements IRateLimiter {

    private final RedissonClient client;

    public RedissonRateLimiter(RedissonClient client) {
        this.client = client;
    }

    private final Map<String, RRateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public Boolean tryAcquire(String key, long capacity, long period) {
        // 获取或创建指定键的速率限制器实例
        RRateLimiter rateLimiter = limiters.computeIfAbsent(key, k -> client.getRateLimiter(key));
        // 设置速率限制器的速率和时间间隔
        rateLimiter.trySetRate(RateType.OVERALL, capacity, Duration.ofSeconds(period));
        // 尝试获取一个许可
        return rateLimiter.tryAcquire();
    }

    @Override
    public void clear() {
        limiters.forEach((key, rateLimiter) -> rateLimiter.delete());
        limiters.clear();
    }
}
