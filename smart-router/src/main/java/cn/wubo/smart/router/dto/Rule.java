package cn.wubo.smart.router.dto;

import cn.wubo.smart.router.SmartRouterProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Rule {
    private List<SmartRouterProperties.RateLimitRule> rateLimitRules = new ArrayList<>();
    private List<SmartRouterProperties.ProxyRule> proxyRules = new ArrayList<>();

}
