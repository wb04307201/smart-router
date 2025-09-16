package cn.wubo.smart.router.interceptor;

import cn.wubo.smart.router.SmartRouterManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class SmartRouterInterceptor implements HandlerInterceptor {

    private final SmartRouterManager smartRouterManager;

    public SmartRouterInterceptor(SmartRouterManager smartRouterManager) {
        this.smartRouterManager = smartRouterManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return smartRouterManager.rule(request, response);
    }
}
