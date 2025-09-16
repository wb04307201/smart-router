package cn.wubo.smart.router.factory;

import cn.wubo.smart.router.SmartRouterProperties;
import cn.wubo.smart.router.bucket.IRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StandaloneFactoryTest {

    private StandaloneFactory standaloneFactory;
    private SmartRouterProperties.RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        standaloneFactory = new StandaloneFactory();
        rateLimiter = new SmartRouterProperties.RateLimiter();
    }

    @Test
    void testSupportsStandaloneType() {
        rateLimiter.setRateLimitingType("standalone");
        assertTrue(standaloneFactory.supports(rateLimiter.getRateLimitingType()));
    }

    @Test
    void testDoesNotSupportOtherTypes() {
        rateLimiter.setRateLimitingType("redis");
        assertFalse(standaloneFactory.supports(rateLimiter.getRateLimitingType()));

        rateLimiter.setRateLimitingType("redis-cluster");
        assertFalse(standaloneFactory.supports(rateLimiter.getRateLimitingType()));
    }

    @Test
    void testCreateReturnsGuavaRateLimiter() {
        IRateLimiter rateLimiterInstance = standaloneFactory.create(rateLimiter);
        assertNotNull(rateLimiterInstance);
        assertTrue(rateLimiterInstance instanceof cn.wubo.smart.router.bucket.GuavaRateLimiter);
    }
}
