package cn.wubo.rate.limit.core.platform.redission;

import cn.wubo.rate.limit.core.platform.IRateLimit;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

public class RedissonRateLimiter implements IRateLimit {

    private final RedissonClient client;

    public RedissonRateLimiter(RedissonClient client) {
        this.client = client;
    }

    /**
     * 尝试获取指定键的速率限制许可。
     *
     * @param key   速率限制器的键标识
     * @param count 速率限制的次数
     * @param time  速率限制的时间间隔（秒）
     * @return 如果成功获取许可则返回true，否则返回false
     */
    @Override
    public boolean tryAcquire(String key, int count, int time) {
        try {
            // 获取或创建指定键的速率限制器实例
            RRateLimiter rateLimiter = client.getRateLimiter(key);

            // 设置速率限制器的速率和时间间隔
            rateLimiter.trySetRate(RateType.OVERALL, count, Duration.ofSeconds(time));

            // 尝试获取一个许可
            return rateLimiter.tryAcquire();
        } catch (Exception e) {
            // 发生异常时，拒绝请求以保证服务稳定性
            return false;
        }
    }
}
