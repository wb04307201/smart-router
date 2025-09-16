package cn.wubo.smart.router.annotation;

import cn.wubo.smart.router.SmartRouterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterValidatorTest {

    private Validator validator;
    private SmartRouterProperties.RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        rateLimiter = new SmartRouterProperties.RateLimiter();
    }

    @Test
    void testValidStandaloneRateLimiter() {
        // 测试默认的 standalone 类型应该通过验证
        rateLimiter.setRateLimitingType("standalone");
        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUnknownRateLimiterType() {
        // 测试未知类型应该通过验证（让其他验证器处理）
        rateLimiter.setRateLimitingType("unknown");
        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidRedisRateLimiter() {
        // 测试有效的 Redis 配置
        rateLimiter.setRateLimitingType("redis");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("address", "localhost:6379");
        attributes.put("password", "password");
        attributes.put("database", 0);
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRedisRateLimiterMissingAddress() {
        // 测试 Redis 配置缺少 address
        rateLimiter.setRateLimitingType("redis");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("password", "password");
        attributes.put("database", 0);
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis address is required"));
    }

    @Test
    void testInvalidRedisRateLimiterInvalidAddressType() {
        // 测试 Redis 配置 address 类型错误
        rateLimiter.setRateLimitingType("redis");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("address", 123); // 应该是字符串
        attributes.put("password", "password");
        attributes.put("database", 0);
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis address is required"));
    }

    @Test
    void testInvalidRedisRateLimiterMissingPassword() {
        // 测试 Redis 配置缺少 password
        rateLimiter.setRateLimitingType("redis");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("address", "localhost:6379");
        attributes.put("database", 0);
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis password is required"));
    }

    @Test
    void testInvalidRedisRateLimiterInvalidDatabaseType() {
        // 测试 Redis 配置 database 类型错误
        rateLimiter.setRateLimitingType("redis");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("address", "localhost:6379");
        attributes.put("password", "password");
        attributes.put("database", "invalid"); // 应该是整数
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis database type is Integer and value between 0~15"));
    }

    @Test
    void testInvalidRedisRateLimiterInvalidDatabaseRange() {
        // 测试 Redis 配置 database 范围错误
        rateLimiter.setRateLimitingType("redis");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("address", "localhost:6379");
        attributes.put("password", "password");
        attributes.put("database", 16); // 超出范围 0-15
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis database type is Integer and value between 0~15"));
    }

    @Test
    void testValidRedisClusterRateLimiter() {
        // 测试有效的 Redis Cluster 配置
        rateLimiter.setRateLimitingType("redis-cluster");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("nodes", Arrays.asList("localhost:7000", "localhost:7001"));
        attributes.put("password", "password");
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRedisClusterRateLimiterMissingNodes() {
        // 测试 Redis Cluster 配置缺少 nodes
        rateLimiter.setRateLimitingType("redis-cluster");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("password", "password");
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis cluster nodes are required"));
    }

    @Test
    void testInvalidRedisClusterRateLimiterInvalidNodesType() {
        // 测试 Redis Cluster 配置 nodes 类型错误
        rateLimiter.setRateLimitingType("redis-cluster");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("nodes", "invalid"); // 应该是列表
        attributes.put("password", "password");
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis cluster nodes are required"));
    }

    @Test
    void testInvalidRedisClusterRateLimiterEmptyNodes() {
        // 测试 Redis Cluster 配置 nodes 为空
        rateLimiter.setRateLimitingType("redis-cluster");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("nodes", new ArrayList<>()); // 空列表
        attributes.put("password", "password");
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis cluster nodes are required"));
    }

    @Test
    void testValidRedisSentinelRateLimiter() {
        // 测试有效的 Redis Sentinel 配置
        rateLimiter.setRateLimitingType("redis-sentinel");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("nodes", Arrays.asList("localhost:26379", "localhost:26380"));
        attributes.put("password", "password");
        attributes.put("masterName", "mymaster");
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRedisSentinelRateLimiterMissingMasterName() {
        // 测试 Redis Sentinel 配置缺少 masterName
        rateLimiter.setRateLimitingType("redis-sentinel");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("nodes", Arrays.asList("localhost:26379", "localhost:26380"));
        attributes.put("password", "password");
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Redis sentinel masterName is required"));
    }

    @Test
    void testMultipleValidationErrors() {
        // 测试多个验证错误
        rateLimiter.setRateLimitingType("redis");
        Map<String, Object> attributes = new HashMap<>();
        // 缺少 address 和 password
        attributes.put("database", 0);
        rateLimiter.setAttributes(attributes);

        Set<ConstraintViolation<SmartRouterProperties.RateLimiter>> violations = validator.validate(rateLimiter);
        // 应该至少有一个错误（address 错误）
        assertFalse(violations.isEmpty());
    }
}
