package cn.wubo.rate.limit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {
    Long warmupPeriod = 0L;// 预热时间
    TimeUnit unit = TimeUnit.NANOSECONDS;// 预热时间单位

}
