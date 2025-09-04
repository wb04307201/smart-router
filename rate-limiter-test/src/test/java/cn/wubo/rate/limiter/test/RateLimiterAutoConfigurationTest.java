package cn.wubo.rate.limiter.test;

import cn.wubo.rate.limiter.autoconfigure.RateLimiterConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RateLimiterConfig.class));

    @Test
    void testAutoConfigurationLoaded() {
        contextRunner
                .withPropertyValues("rate.limiter.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RateLimiterConfig.class);
                });
    }

    @Test
    void testFactoriesBeanExists() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("factories");
            assertThat(context.getBean("factories")).isInstanceOf(java.util.List.class);
        });
    }

    @Test
    void testRouterFunctionBeanExists() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("wb04307201RateLimterRouter");
        });
    }
}
