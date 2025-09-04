package cn.wubo.rate.limiter.bucket;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuavaRateLimiter implements IRateLimiter {

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public Boolean tryAcquire(String key, long capacity, long period) {
        RateLimiter limiter = limiters.computeIfAbsent(key, k -> RateLimiter.create((double) capacity / period));
        return limiter.tryAcquire();
    }

    @Override
    public void clear() {
        limiters.clear();
    }
}
