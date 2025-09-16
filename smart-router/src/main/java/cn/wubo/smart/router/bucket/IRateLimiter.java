package cn.wubo.smart.router.bucket;


public interface IRateLimiter {


    Boolean tryAcquire(String key, long capacity, long period);

    void clear();
}
