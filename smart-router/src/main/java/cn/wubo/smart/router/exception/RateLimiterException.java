package cn.wubo.smart.router.exception;

public class RateLimiterException extends RuntimeException{

    public RateLimiterException(String message) {
        super(message);
    }
}
