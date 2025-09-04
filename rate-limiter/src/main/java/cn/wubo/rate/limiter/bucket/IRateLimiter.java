package cn.wubo.rate.limiter.bucket;


public interface IRateLimiter {


    Boolean tryAcquire(String key, long capacity, long period);

    void clear();
}
