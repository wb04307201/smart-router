package cn.wubo.rate.limit.core;

import cn.wubo.rate.limit.core.platform.IRateLimit;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;

public class RedissionRateLimiter implements IRateLimit {

    private RedissonClient client;

    public RedissionRateLimiter(RedissonClient client) {
        this.client = client;
    }

    @Override
    public boolean tryAcquire(String key, int count, int time) {
        return false;
    }
}
