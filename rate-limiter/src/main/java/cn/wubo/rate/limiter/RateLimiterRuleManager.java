package cn.wubo.rate.limiter;

import cn.wubo.rate.limiter.bucket.IRateLimiter;
import cn.wubo.rate.limiter.storage.IStorage;
import cn.wubo.rate.limiter.storage.RateLimiterInfo;
import cn.wubo.rate.limiter.storage.SimpleStorage;
import lombok.Getter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RateLimiterRuleManager {

    @Getter
    private List<RateLimiterProperties.RateLimitRule> rules;
    private final IRateLimiter bucket;
    private Long systemStartTime;
    private final IStorage storage;

    // 复用 PathMatcher 实例
    private final PathMatcher MATCHER;

    public RateLimiterRuleManager(RateLimiterProperties properties, IRateLimiter bucket, IStorage storage) {
        this.rules = properties.getRules();
        this.bucket = bucket;
        this.systemStartTime = System.currentTimeMillis();
        this.storage = storage;
        this.MATCHER = new AntPathMatcher();
    }

    public Boolean tryConsume(String endpoint) {
        Boolean allow = Boolean.TRUE;
        RateLimiterInfo rateLimiterInfo = new RateLimiterInfo(endpoint, LocalDateTime.now());

        Optional<RateLimiterProperties.RateLimitRule> ruleOptional = rules
                .stream()
                .filter(item -> MATCHER.match(item.getEndpoint(), endpoint))
                .findFirst();

        if (ruleOptional.isPresent()) {
            RateLimiterProperties.RateLimitRule rule = ruleOptional.get();
            allow = bucket.tryAcquire(endpoint, rule.getCapacity(), rule.getPeriod()); // 判断是否允许执行
        }

        rateLimiterInfo.setAllowed(allow);
        storage.add(rateLimiterInfo);

        return allow;
    }

    public List<RateLimiterInfo> getAll() {
        return storage.getAll();
    }

    public Boolean updateRules(List<RateLimiterProperties.RateLimitRule> rules) {
        bucket.clear();
        this.rules = rules;
        storage.clear();
        this.systemStartTime = System.currentTimeMillis();
        return true;
    }

    public List<Map<String, Object>> getStatic() {
        // 按URI分组统计总请求数
        Map<String, Long> totalRequests = storage.getAll().stream()
                .collect(Collectors.groupingBy(RateLimiterInfo::getEndpoint, Collectors.counting()));

        // 按URI分组统计允许请求数
        Map<String, Long> allowedRequests = storage.getAll().stream()
                .filter(RateLimiterInfo::getAllowed)
                .collect(Collectors.groupingBy(RateLimiterInfo::getEndpoint, Collectors.counting()));

        // 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();

        totalRequests.forEach((endpoint, total) -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("endpoint", endpoint);
            stats.put("isRateLimiter",
                    rules
                            .stream()
                            .anyMatch(item -> MATCHER.match(item.getEndpoint(), endpoint))
            );
            stats.put("total", total);
            stats.put("allowed", allowedRequests.getOrDefault(endpoint, 0L));
            stats.put("allowedRate", total > 0 ? (allowedRequests.getOrDefault(endpoint, 0L) / total) * 100 : 0L);
            stats.put("qps", total / ((System.currentTimeMillis() - systemStartTime) / 1000D));
            result.add(stats);
        });

        return result;
    }

    public List<Map<String, Object>> getStaticByEndpoint(String endpoint) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH");

        List<RateLimiterInfo> targets = storage.getByEndpoint(endpoint);

        Map<String, Long> totalRequests = targets.stream()
                .collect(Collectors.groupingBy(info -> info.getTimestamp().format(dtf), Collectors.counting()));

        Map<String, Long> allowedRequests = targets.stream()
                .filter(RateLimiterInfo::getAllowed)
                .collect(Collectors.groupingBy(info -> info.getTimestamp().format(dtf), Collectors.counting()));


        // 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();

        totalRequests.forEach((hour, total) -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("hour", hour);
            stats.put("total", total);
            stats.put("allowed",
                    rules
                            .stream()
                            .anyMatch(item -> MATCHER.match(item.getEndpoint(), endpoint)) ? allowedRequests.getOrDefault(hour, 0L) : total

            );
            result.add(stats);
        });

        return result.stream()
                .sorted(Comparator.comparing(stats -> (String) stats.get("hour")))
                .toList();
    }

}
