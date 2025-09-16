package cn.wubo.smart.router.autoconfigure;

import cn.wubo.smart.router.SmartRouterManager;
import cn.wubo.smart.router.SmartRouterProperties;
import cn.wubo.smart.router.bucket.IRateLimiter;
import cn.wubo.smart.router.dto.Rule;
import cn.wubo.smart.router.factory.IFactory;
import cn.wubo.smart.router.factory.RedisFactory;
import cn.wubo.smart.router.factory.StandaloneFactory;
import cn.wubo.smart.router.interceptor.SmartRouterInterceptor;
import cn.wubo.smart.router.storage.IStorage;
import cn.wubo.smart.router.storage.SimpleStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({SmartRouterProperties.class})
public class SmartRouterConfig implements WebMvcConfigurer {

    private final ObjectProvider<SmartRouterInterceptor> rateLimiterInterceptorProvider;

    public SmartRouterConfig(ObjectProvider<SmartRouterInterceptor> rateLimiterInterceptorProvider) {
        this.rateLimiterInterceptorProvider = rateLimiterInterceptorProvider;
    }

    @Bean
    @ConditionalOnMissingBean
    public IStorage simpleStorage() {
        return new SimpleStorage();
    }

    @Bean
    public List<IFactory> factories() {
        return List.of(new StandaloneFactory(), new RedisFactory());
    }

    @Bean
    public IRateLimiter bucket(SmartRouterProperties properties, List<IFactory> factories) {
        return factories.stream()
                .filter(factory -> factory.supports(properties.getRateLimiter().getRateLimitingType()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported rateLimiting type: " + properties.getRateLimiter().getRateLimitingType()))
                .create(properties.getRateLimiter());
    }

    @Bean
    public SmartRouterManager rateLimitingRuleManager(SmartRouterProperties properties, IRateLimiter bucket, IStorage storage) {
        return new SmartRouterManager(properties, bucket, storage);
    }

    @Bean
    public SmartRouterInterceptor rateLimitInterceptor(SmartRouterManager ruleManager) {
        return new SmartRouterInterceptor(ruleManager);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimiterInterceptorProvider.getObject())
                .addPathPatterns("/**");
    }

    @Bean("wb04307201SmartRouter")
    public RouterFunction<ServerResponse> methodTraceLogRouter(SmartRouterManager smartRouterManager, IStorage storage) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        builder.GET("/smart/router/monitor/view", request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(new ClassPathResource(("/monitor.html"))));
        builder.GET("/smart/router/monitor/static", request -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(storage.getAllStatic()));
        builder.GET("/smart/router/monitor/rules", request -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(smartRouterManager.getRules()));
        builder.POST("/smart/router/monitor/rules", request -> {
            Rule rule = request.body(new ParameterizedTypeReference<>() {
            });
            smartRouterManager.updateRules(rule);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(true);
        });

        return builder.build();
    }
}
