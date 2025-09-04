package cn.wubo.rate.limiter.test;

import cn.wubo.rate.limiter.RateLimiterProperties;
import cn.wubo.rate.limiter.RateLimiterRuleManager;
import cn.wubo.rate.limiter.autoconfigure.RateLimiterConfig;
import cn.wubo.rate.limiter.bucket.IRateLimiter;
import cn.wubo.rate.limiter.factory.IFactory;
import cn.wubo.rate.limiter.factory.RedisFctory;
import cn.wubo.rate.limiter.factory.StandaloneFctory;
import cn.wubo.rate.limiter.interceptor.RateLimiterInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterConfigTest {

    @Mock
    private ObjectProvider<RateLimiterInterceptor> rateLimiterInterceptorProvider;

    @Mock
    private RateLimiterInterceptor rateLimiterInterceptor;

    @Test
    void testFactoriesBean() {
        // Given
        RateLimiterConfig config = new RateLimiterConfig(rateLimiterInterceptorProvider);

        // When
        List<IFactory> factories = config.factories();

        // Then
        assertNotNull(factories);
        assertEquals(2, factories.size());
        assertTrue(factories.get(0) instanceof StandaloneFctory);
        assertTrue(factories.get(1) instanceof RedisFctory);
    }

    @Test
    void testBucketBeanWithStandaloneFactory() {
        // Given
        RateLimiterConfig config = new RateLimiterConfig(rateLimiterInterceptorProvider);
        RateLimiterProperties properties = new RateLimiterProperties();
        properties.setRateLimitingType("standalone");

        List<IFactory> factories = config.factories();

        // When
        IRateLimiter bucket = config.bucket(properties, factories);

        // Then
        assertNotNull(bucket);
    }

    @Test
    void testBucketBeanWithUnsupportedFactory() {
        // Given
        RateLimiterConfig config = new RateLimiterConfig(rateLimiterInterceptorProvider);
        RateLimiterProperties properties = new RateLimiterProperties();
        properties.setRateLimitingType("unsupported");

        List<IFactory> factories = config.factories();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            config.bucket(properties, factories);
        });
    }

    @Test
    void testRateLimitInterceptorBean() {
        // Given
        RateLimiterConfig config = new RateLimiterConfig(rateLimiterInterceptorProvider);
        RateLimiterRuleManager ruleManager = mock(RateLimiterRuleManager.class);

        // When
        RateLimiterInterceptor interceptor = config.rateLimitInterceptor(ruleManager);

        // Then
        assertNotNull(interceptor);
    }

    @Test
    void testMethodTraceLogRouterBean() {
        // Given
        RateLimiterConfig config = new RateLimiterConfig(rateLimiterInterceptorProvider);
        RateLimiterRuleManager ruleManager = mock(RateLimiterRuleManager.class);

        // When
        RouterFunction<ServerResponse> router = config.methodTraceLogRouter(ruleManager);

        // Then
        assertNotNull(router);
    }
}
