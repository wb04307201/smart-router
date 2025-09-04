package cn.wubo.rate.limiter.interceptor;

import cn.wubo.rate.limiter.RateLimiterRuleManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimiterInterceptor implements HandlerInterceptor {

    private  final RateLimiterRuleManager ruleManager;

    public RateLimiterInterceptor(RateLimiterRuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String endpoint = request.getRequestURI();
        if (!ruleManager.tryConsume(endpoint)) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
            return false;
        }
        return true;
    }
}
