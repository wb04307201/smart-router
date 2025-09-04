package cn.wubo.rate.limiter.autoconfigure;

import cn.wubo.rate.limiter.interceptor.RateLimiterInterceptor;
import cn.wubo.rate.limiter.RateLimiterProperties;
import cn.wubo.rate.limiter.RateLimiterRuleManager;
import cn.wubo.rate.limiter.bucket.IRateLimiter;
import cn.wubo.rate.limiter.factory.IFactory;
import cn.wubo.rate.limiter.factory.RedisFactory;
import cn.wubo.rate.limiter.factory.StandaloneFactory;
import cn.wubo.rate.limiter.storage.IStorage;
import cn.wubo.rate.limiter.storage.SimpleStorage;
import jakarta.validation.Valid;
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
@EnableConfigurationProperties({RateLimiterProperties.class})
public class RateLimiterConfig implements WebMvcConfigurer {

    private final ObjectProvider<RateLimiterInterceptor> rateLimiterInterceptorProvider;

    public RateLimiterConfig(ObjectProvider<RateLimiterInterceptor> rateLimiterInterceptorProvider) {
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
    public IRateLimiter bucket(@Valid RateLimiterProperties properties, List<IFactory> factories) {
        return factories.stream()
                .filter(factory -> factory.supports(properties.getRateLimitingType()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported rateLimiting type: " + properties.getRateLimitingType()))
                .create(properties, null);
    }

    @Bean
    public RateLimiterRuleManager rateLimitingRuleManager(RateLimiterProperties properties, IRateLimiter bucket, IStorage storage) {
        return new RateLimiterRuleManager(properties, bucket, storage);
    }

    @Bean
    public RateLimiterInterceptor rateLimitInterceptor(RateLimiterRuleManager ruleManager) {
        return new RateLimiterInterceptor(ruleManager);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimiterInterceptorProvider.getObject())
                .addPathPatterns("/**");
    }

    @Bean("wb04307201RateLimterRouter")
    public RouterFunction<ServerResponse> methodTraceLogRouter(RateLimiterRuleManager ruleManager) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        builder.GET("/rate/limiter/monitor/view", request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(new ClassPathResource(("/monitor.html"))));
        builder.GET("/rate/limiter/monitor/static", request -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ruleManager.getStatic()));
        builder.GET("/rate/limiter/monitor/staticByEndpoint", request -> {
                    String endpoint = request.param("endpoint").orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "endpoint is required"));
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ruleManager.getStaticByEndpoint(endpoint));
                }
        );
        builder.GET("/rate/limiter/monitor/rules", request -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ruleManager.getRules()));
        builder.POST("/rate/limiter/monitor/rules", request -> {
            List<RateLimiterProperties.RateLimitRule> rules = request.body(new ParameterizedTypeReference<>() {
            });
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ruleManager.updateRules(rules));
        });

        return builder.build();
    }
}
