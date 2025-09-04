package cn.wubo.rate.limit.exception;

public class RateLimiterException extends RuntimeException{

    public RateLimiterException(String message) {
        super(message);
    }
}
