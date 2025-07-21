package cn.wubo.rate.limit.core.platform;

public interface IRateLimit {

    boolean tryAcquire(String key,int count,int time);
}
