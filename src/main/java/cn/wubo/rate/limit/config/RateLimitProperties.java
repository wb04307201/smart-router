package cn.wubo.rate.limit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {
    // guava redis redis-cluster redis-sentinel
    private String rateLimitType = "guava";

    private RedisProperties redis = new RedisProperties();

    @Data
    public class RedisProperties {
        // 单例地址
        private String address = "localhost:6379";
        // 密码
        private String password;
        // 数据库
        private Integer database = 0;
        // 集群、哨兵节点
        private List<String> nodes;
        // 烧饼主节点名
        private String masterName;
    }

}
