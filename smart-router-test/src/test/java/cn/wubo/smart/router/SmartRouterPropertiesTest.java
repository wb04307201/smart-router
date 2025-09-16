package cn.wubo.smart.router;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class SmartRouterPropertiesTest {

    private SmartRouterProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SmartRouterProperties();
    }

    @Test
    void testDefaultValues() {
        // 测试默认值
        assertNotNull(properties.getRateLimiter());
        assertEquals("standalone", properties.getRateLimiter().getRateLimitingType());
        assertNotNull(properties.getRateLimitRules());
        assertNotNull(properties.getProxyRules());
        assertTrue(properties.getRateLimitRules().isEmpty());
        assertTrue(properties.getProxyRules().isEmpty());
    }

    @Test
    void testRateLimitRule() {
        SmartRouterProperties.RateLimitRule rule = new SmartRouterProperties.RateLimitRule();
        rule.setEndpoint("/api/test");
        rule.setCapacity(100);
        rule.setPeriod(60);
        rule.setUnit(TimeUnit.MINUTES);

        assertEquals("/api/test", rule.getEndpoint());
        assertEquals(100, rule.getCapacity());
        assertEquals(60, rule.getPeriod());
        assertEquals(TimeUnit.MINUTES, rule.getUnit());
    }

    @Test
    void testProxyRule() {
        SmartRouterProperties.ProxyRule proxyRule = new SmartRouterProperties.ProxyRule();
        proxyRule.setEndpoint("/api/proxy");

        assertNotNull(proxyRule.getProxies());
        assertTrue(proxyRule.getProxies().isEmpty());
        assertEquals("/api/proxy", proxyRule.getEndpoint());
    }
}
