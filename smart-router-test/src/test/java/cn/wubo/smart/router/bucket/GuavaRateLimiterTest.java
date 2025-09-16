package cn.wubo.smart.router.bucket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuavaRateLimiterTest {

    private GuavaRateLimiter guavaRateLimiter;

    @BeforeEach
    void setUp() {
        guavaRateLimiter = new GuavaRateLimiter();
    }

    @Test
    void testTryAcquireSuccess() {
        boolean result = guavaRateLimiter.tryAcquire("test-key", 10, 1);
        assertTrue(result, "首次请求应该成功");
    }

    @Test
    void testTryAcquireFailure() throws InterruptedException {
        // 设置非常高的速率限制
        String key = "high-limit-key";
        for (int i = 0; i < 100; i++) {
            guavaRateLimiter.tryAcquire(key, 1, 1000); // 1个令牌/秒
        }

        boolean result = guavaRateLimiter.tryAcquire(key, 1, 1000);
        assertFalse(result, "超过限制的请求应该失败");
    }

    @Test
    void testClear() {
        guavaRateLimiter.tryAcquire("key1", 5, 1);
        guavaRateLimiter.tryAcquire("key2", 5, 1);

        guavaRateLimiter.clear();

        // 重新请求应该成功，因为 limiter 已被清除
        boolean result = guavaRateLimiter.tryAcquire("key1", 5, 1);
        assertTrue(result);
    }
}
