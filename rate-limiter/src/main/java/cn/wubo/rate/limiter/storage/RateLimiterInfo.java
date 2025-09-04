package cn.wubo.rate.limiter.storage;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RateLimiterInfo {
    private String endpoint;
    private LocalDateTime timestamp;
    private Boolean allowed;

    public RateLimiterInfo(String endpoint, LocalDateTime timestamp) {
        this.endpoint = endpoint;
        this.timestamp = timestamp;
    }
}
