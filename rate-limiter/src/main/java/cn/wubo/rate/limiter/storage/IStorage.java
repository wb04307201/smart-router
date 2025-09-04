package cn.wubo.rate.limiter.storage;

import java.util.List;

public interface IStorage {
    void add(RateLimiterInfo rateLimiterInfo);
    List<RateLimiterInfo> getAll();
    List<RateLimiterInfo> getByEndpoint(String endpoint);
    void clear();
}
