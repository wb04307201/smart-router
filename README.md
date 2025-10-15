# Smart Router

<div align="right">
  English | <a href="README.zh-CN.md">中文</a>
</div>

> A Spring Boot-based intelligent routing and rate limiting component that supports multiple rate limiting strategies and routing rule management, enabling simple grayscale publishing.

[![](https://jitpack.io/v/com.gitee.wb04307201/smart-router.svg)](https://jitpack.io/#com.gitee.wb04307201/smart-router)
[![star](https://gitee.com/wb04307201/smart-router/badge/star.svg?theme=dark)](https://gitee.com/wb04307201/smart-router)
[![fork](https://gitee.com/wb04307201/smart-router/badge/fork.svg?theme=dark)](https://gitee.com/wb04307201/smart-router)
[![star](https://img.shields.io/github/stars/wb04307201/smart-router)](https://github.com/wb04307201/smart-router)
[![fork](https://img.shields.io/github/forks/wb04307201/smart-router)](https://github.com/wb04307201/smart-router)  
![MIT](https://img.shields.io/badge/License-Apache2.0-blue.svg) ![JDK](https://img.shields.io/badge/JDK-17+-green.svg) ![SpringBoot](https://img.shields.io/badge/Spring%20Boot-3+-green.svg)

## Features

- Support for multiple rate limiting algorithms:
    - Google Guava Token Bucket Algorithm
    - Redisson Distributed Rate Limiting
- Support for multiple Redis deployment modes:
    - Single-node Redis
    - Redis Cluster Mode
    - Redis Sentinel Mode
- Dynamic Routing Rule Management
- Real-time Monitoring Panel
- Annotation-based Rate Limiting Configuration
- Spring Boot Auto Configuration

## Add JitPack Repository
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Import Dependency
```xml
<dependency>
    <groupId>com.gitee.wb04307201.smart-router</groupId>
    <artifactId>smart-router-spring-boot-starter</artifactId>
    <version>1.0.5</version>
</dependency>
```

## Basic Configuration Structure

```yaml
smart-router:
  rateLimiter:
    rateLimitingType: standalone # Rate limiting type: standalone, redis, redis-cluster, redis-sentinel
  rateLimitRules:                # Rate limiting rules list
    - endpoint: /api/test        # API endpoint
      capacity: 100              # Capacity/token count
      period: 60                 # Time period
      unit: SECONDS              # Time unit (optional, defaults to SECONDS)
  proxyRules:                    # Proxy rules list
    - endpoint: /api/proxy       # Proxy endpoint
      proxies:                   # Target proxy list
        - targetEndpoint: /api/v1/test  # Target endpoint
          weight: 5              # Weight
        - targetEndpoint: /api/v2/test
          weight: 5
```


## Rate Limiting Type Configuration

### 1. Standalone Mode (Local Rate Limiting)
```yaml
smart-router:
  rateLimiter:
    rateLimitingType: standalone
```


### 2. Redis Mode
```yaml
smart-router:
  rateLimiter:
    rateLimitingType: redis
    attributes:
      address: localhost:6379    # Redis address
      password: your_password     # Redis password
      database: 0                # Database index (0-15)
```


### 3. Redis Cluster Mode
```yaml
smart-router:
  rateLimiter:
    rateLimitingType: redis-cluster
    attributes:
      nodes:                    # Cluster node list
        - localhost:7000
        - localhost:7001
      password: your_password    # Password
```


### 4. Redis Sentinel Mode
```yaml
smart-router:
  rateLimiter:
    rateLimitingType: redis-sentinel
    attributes:
      nodes:                    # Sentinel node list
        - localhost:26379
        - localhost:26380
      password: your_password    # Password
      masterName: mymaster       # Master node name
```


## Complete Configuration Example

```yaml
smart-router:
  # Rate limiting rules
  rateLimitRules:
    - endpoint: /test/hello
      capacity: 1     # Only 1 request allowed per 10 seconds
      period: 10
      unit: SECONDS   # Time unit (optional)
  
  # Proxy rules
  proxyRules:
    - endpoint: /test/version
      proxies:
        - targetEndpoint: /test/v1/version
          weight: 5    # 50% traffic
        - targetEndpoint: /test/v2/version
          weight: 5    # 50% traffic
          
  # Redis-related configuration (choose according to the rate limiting type used)
  rateLimiter:
    # Rate limiting type
    rateLimitingType: redis
    attributes:
      # Redis standalone configuration
      address: localhost:6379
      password: password
      database: 0
      
      # Or Redis Cluster configuration
      # nodes:
      #   - localhost:7000
      #   - localhost:7001
      # password: password
      
      # Or Redis Sentinel configuration
      # nodes:
      #   - localhost:26379
      #   - localhost:26380
      # password: password
      # masterName: mymaster
```

## Monitoring Functionality

The project provides a built-in monitoring page. Visit `/smart/router/monitor/view` to view the monitoring page.
![img.png](img.png)
![img_1.png](img_1.png)


## Extensibility

The project is designed with good extensibility:
1. New rate limiting algorithms can be added by implementing the [IFactory.java](smart-router/src/main/java/cn/wubo/smart/router/factory/IFactory.java) and [IRateLimiter.java](smart-router/src/main/java/cn/wubo/smart/router/bucket/IRateLimiter.java) interfaces
2. Custom storage can be implemented by implementing the [IStorage.java](smart-router/src/main/java/cn/wubo/smart/router/storage/IStorage.java) interface