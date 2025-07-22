package cn.wubo.rate.limit.exception;

public class RateLimitException extends RuntimeException{

    public RateLimitException(String message) {
        super(message);
    }
}
