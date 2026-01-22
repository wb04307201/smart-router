# Smart Router

<div align="right">
  English | <a href="README.zh-CN.md">中文</a>
</div>

> A Spring Boot component that provides request path-based rate limiting, proxying, parameter modification, and request body modification within applications.

[![](https://jitpack.io/v/com.gitee.wb04307201/smart-router.svg)](https://jitpack.io/#com.gitee.wb04307201/smart-router)
[![star](https://gitee.com/wb04307201/smart-router/badge/star.svg?theme=dark)](https://gitee.com/wb04307201/smart-router)
[![fork](https://gitee.com/wb04307201/smart-router/badge/fork.svg?theme=dark)](https://gitee.com/wb04307201/smart-router)
[![star](https://img.shields.io/github/stars/wb04307201/smart-router)](https://github.com/wb04307201/smart-router)
[![fork](https://img.shields.io/github/forks/wb04307201/smart-router)](https://github.com/wb04307201/smart-router)  
![MIT](https://img.shields.io/badge/License-Apache2.0-blue.svg) ![JDK](https://img.shields.io/badge/JDK-17+-green.svg) ![SpringBoot](https://img.shields.io/badge/Spring%20Boot-3+-green.svg)

## Features

- Rate Limiting
  - Multiple rate limiting algorithms support:
    - Google Guava Token Bucket Algorithm
    - Redisson Distributed Rate Limiting
        - Multiple Redis deployment modes support:
          - Single Node Redis
          - Redis Cluster Mode
          - Redis Sentinel Mode
- Proxy
    - Supports weight configuration
    - SpEL Expression Parameter Modification
    - SpEL Expression Request Body Modification
- Dynamic rate limiting and proxy configuration route modification
- Real-time monitoring dashboard

## Add JitPack Repository
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Include the jar
```xml
<dependency>
    <groupId>com.gitee.wb04307201.smart-router</groupId>
    <artifactId>smart-router-spring-boot-starter</artifactId>
    <version>1.0.6</version>
</dependency>
```

## Basic Configuration Structure

```yaml
smart-router:
  rateLimiter:
    rateLimitingType: standalone                      # Rate limiting type: standalone, redis, redis-cluster, redis-sentinel
    rateLimitRules:                                   # Rate limiting rules list
      - endpoint: /test/hello                         # Rate limiting path
        capacity: 1                                   # Capacity/token count
        period: 10                                    # Time period
        unit: SECONDS                                 # Time unit (optional, default SECONDS)
  proxyRules:                                         # Proxy rules list
    - endpoint: /test/get                             # Proxy path
      proxies:                                        # Proxy targets list
        - targetEndpoint: /test/v1/get                # Target path
          weight: 5                                   # Weight
                                                      # Parameter mapping
          mapRule: |-
            #params.put('flagv1',#params.get('flag'))
        - targetEndpoint: /test/v2/get
          weight: 5
          mapRule: |-
            #params.put('flagv2',#params.get('flag'))
    - endpoint: /test/post
      proxies:
        - targetEndpoint: /test/v1/post
          weight: 5
                                                        # Request body mapping
          bodyRule: |-
            #params.put('value1','Hello')
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

## Runtime Dynamic Modification of Rate Limiting and Proxy Configuration
Get current rate limiting and proxy configuration
```http request
GET http://localhost:8080/smart/router/monitor/rules
Accept: application/json
```
Response
```json
{
    "rateLimitRules": [  //Rate limiting rules
        {
            "endpoint": "/test/hello",
            "capacity": 1,
            "period": 10,
            "unit": "SECONDS"
        }
    ],
    "proxyRules": [  //Proxy rules
        {
            "endpoint": "/test/get",
            "proxies": [
                {
                    "targetEndpoint": "/test/v1/get",
                    "weight": 5,
                    "mapRule": "#params.put('flagv1',#params.get('flag'))",
                    "bodyRule": null
                },
                {
                    "targetEndpoint": "/test/v2/get",
                    "weight": 5,
                    "mapRule": "#params.put('flagv2',#params.get('flag'))",
                    "bodyRule": null
                }
            ]
        },
        {
            "endpoint": "/test/post",
            "proxies": [
                {
                    "targetEndpoint": "/test/v1/post",
                    "weight": 10,
                    "mapRule": null,
                    "bodyRule": "#params.put('value1','Hello')"
                }
            ]
        }
    ]
}
```

Modify rate limiting and proxy configuration
```http request
POST http://localhost:8080/smart/router/monitor/rules
Content-Type: application/json



```

## Monitoring Dashboard

The project provides a built-in monitoring page. Access `/smart/router/monitor/view` to view the monitoring page
![img.png](img.png)
![img_1.png](img_1.png)

You can view and adjust rate limiting configuration and proxy configuration on the page
![img_2.png](img_2.png)


## Extensibility

The project design has good extensibility:
1. New rate limiting algorithms can be added by implementing the [IFactory.java](smart-router/src/main/java/cn/wubo/smart/router/factory/IFactory.java) and [IRateLimiter.java](smart-router/src/main/java/cn/wubo/smart/router/bucket/IRateLimiter.java) interfaces
2. Custom storage can be implemented by implementing the [IStorage.java](smart-router/src/main/java/cn/wubo/smart/router/storage/IStorage.java) interface