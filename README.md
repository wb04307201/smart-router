# rate-limit-spring-boot-starter

[![](https://jitpack.io/v/com.gitee.wb04307201/rate-limit-spring-boot-starter.svg)](https://jitpack.io/#com.gitee.wb04307201/rate-limit-spring-boot-starter)
[![star](https://gitee.com/wb04307201/rate-limit-spring-boot-starter/badge/star.svg?theme=dark)](https://gitee.com/wb04307201/rate-limit-spring-boot-starter)
[![fork](https://gitee.com/wb04307201/rate-limit-spring-boot-starter/badge/fork.svg?theme=dark)](https://gitee.com/wb04307201/rate-limit-spring-boot-starter)
[![star](https://img.shields.io/github/stars/wb04307201/rate-limit-spring-boot-starter)](https://github.com/wb04307201/rate-limit-spring-boot-starter)
[![fork](https://img.shields.io/github/forks/wb04307201/rate-limit-spring-boot-starter)](https://github.com/wb04307201/rate-limit-spring-boot-starter)  
![MIT](https://img.shields.io/badge/License-Apache2.0-blue.svg) ![JDK](https://img.shields.io/badge/JDK-17+-green.svg) ![SpringBoot](https://img.shields.io/badge/Srping%20Boot-3+-green.svg)

> 一个注解@RateLimit搞定限流
> 通过或配置可切换布式与单节模式
> 分布式模式下需要借助redis实现分布式限流

## 第一步 增加 JitPack 仓库
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## 第二步 引入jar
```xml
<dependency>
    <groupId>com.gitee.wb04307201</groupId>
    <artifactId>rate-limit-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 第三步 在启动类上加上`@EnableRateLimit.java`注解
```java
@EnableRateLimit
@SpringBootApplication
public class RateLimitDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateLimitDemoApplication.class, args);
    }

}
```

## 第四步 通过注解使用锁
默认使用guava的令牌桶计算限流
```java
@RestController
public class DemoController {

    @RateLimit(count = 1, time = 10)
    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        return "hello world";
    }
}
```



## 在`application.yml`配置文件中配置使用redis的分布式限流
```yaml
rate:
  limit:
      rateLimitType: redis // 可选值 redis（单点） redis-cluster（集群） redis-sentinel（哨兵）
      // redis 配置示例
      redis:
        address: redis://ip:port
        password: mypassword
        # 数据库，默认是0
        database: 0
      // 集群配置示例
      redis:
        password: mypassword
        # 集群节点
        nodes:
          - redis://ip:port
          - redis://ip:port
          - redis://ip:port
      // 哨兵配置示例
      redis:
        password: mypassword
        # 数据库，默认0
        database: 0
        # 集群节点
        nodes:
          - redis://ip:port
          - redis://ip:port
          - redis://ip:port
        # 主服务名
        masterName: masterName
    
```

