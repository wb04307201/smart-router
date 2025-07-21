package cn.wubo.rate.limit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int count() default 100; // 每个窗口允许请求数，默认100
    int time() default 1;  // 时间窗口(秒)，默认1秒
    String message() default "系统繁忙，请稍后再试!"; // 限流提示
}
