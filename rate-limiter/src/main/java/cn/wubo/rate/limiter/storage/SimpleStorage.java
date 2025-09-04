package cn.wubo.rate.limiter.storage;

import java.util.ArrayList;
import java.util.List;

public class SimpleStorage implements IStorage {

    private List<RateLimiterInfo> rateLimiterInfos = new ArrayList<>();

    @Override
    public void add(RateLimiterInfo rateLimiterInfo) {
        if(rateLimiterInfos.size() >1000) rateLimiterInfos.remove(0);
        rateLimiterInfos.add(rateLimiterInfo);
    }

    @Override
    public List<RateLimiterInfo> getAll() {
        return rateLimiterInfos;
    }

    @Override
    public List<RateLimiterInfo> getByEndpoint(String endpoint) {
        return rateLimiterInfos.stream()
                .filter(rateLimiterInfo -> rateLimiterInfo.getEndpoint().equals(endpoint))
                .toList();
    }

    @Override
    public void clear() {
        rateLimiterInfos.clear();
    }
}
