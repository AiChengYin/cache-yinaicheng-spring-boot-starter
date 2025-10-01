# 自封装缓存springbootstarter# Cache Yinaicheng Spring Boot Starter

一个基于 Spring Boot 的自研缓存与系统基础功能封装的 Starter，提供缓存管理、分布式限流、安全验签等核心功能。

## 功能特性

- 🚀 **系统缓存**：支持方法级缓存、自动失效、条件缓存等
- 🔒 **分布式限流**：支持基于 IP、自定义 Key 的限流策略
- 🛡️ **安全验签**：提供接口调用的签名校验机制
- ⚡ **高性能**：基于 Redis 和 J2Cache 的多级缓存架构
- 🎯 **易使用**：注解驱动，无侵入集成
- 🔧 **可配置**：丰富的配置选项，支持个性化定制

## 快速开始

### 1. 添加依赖

在您的项目 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>top.yinaicheng</groupId>
    <artifactId>cache-yinaicheng-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在 `application.properties` 或 `application.yml` 中添加配置：

```properties
# 启用功能
yinaicheng.cache.enabled=true
yinaicheng.limit.enabled=true
yinaicheng.security.enabled=true

# Redis 配置
yinaicheng.cache.redis.host=localhost
yinaicheng.cache.redis.port=6379
yinaicheng.cache.redis.database=0

# 安全密钥
yinaicheng.security.private-key=your_secret_key
```

### 3. 使用示例

#### 缓存功能

```java
@Service
public class UserService {
    
    // 查询缓存
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        duration = 30,
        cacheOperateType = CachedOperationTypeEnum.QUERY_CACHE
    )
    public User getUserById(Long userId) {
        // 从数据库查询用户
        return userRepository.findById(userId);
    }
    
    // 删除缓存
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        cacheOperateType = CachedOperationTypeEnum.DELETE_CACHE_BY_KEY
    )
    public void updateUser(Long userId, User user) {
        userRepository.update(user);
    }
}
```

#### 限流功能

```java
@RestController
public class ApiController {
    
    // IP 限流
    @DistributedLimitTrafficAnnotation(
        description = "API访问限流",
        period = 60,
        count = 100,
        distributedLimitTrafficTypeEnum = DistributedLimitTrafficTypeEnum.REQUESTER_IP
    )
    @GetMapping("/api/data")
    public Result getData() {
        return Result.success();
    }
    
    // 自定义Key限流
    @DistributedLimitTrafficAnnotation(
        description = "用户访问限流",
        key = "#userId",
        period = 60,
        count = 10,
        distributedLimitTrafficTypeEnum = DistributedLimitTrafficTypeEnum.CUSTOM_KEY
    )
    @GetMapping("/api/user/{userId}")
    public Result getUserInfo(@PathVariable Long userId) {
        return Result.success();
    }
}
```

#### 安全验签功能

```java
@RestController
public class SecureController {
    
    @SecurityVerificySignAnnotation(
        enableTimestamp = true,
        enableNonce = true,
        enableSign = true,
        message = "签名验证失败"
    )
    @PostMapping("/api/secure")
    public Result secureApi(@RequestBody String data) {
        return Result.success();
    }
}
```

## 详细配置

### 缓存配置

```properties
# 缓存基础配置
yinaicheng.cache.enabled=true
yinaicheng.cache.default-expiration=30
yinaicheng.cache.key-prefix=cache:
yinaicheng.cache.cache-null-values=false
yinaicheng.cache.max-capacity=10000

# 本地缓存配置
yinaicheng.cache.local.enabled=true
yinaicheng.cache.local.max-size=1000
yinaicheng.cache.local.expire-after-write=10

# Redis缓存配置
yinaicheng.cache.redis.enabled=true
yinaicheng.cache.redis.host=localhost
yinaicheng.cache.redis.port=6379
yinaicheng.cache.redis.database=0
yinaicheng.cache.redis.timeout=2000
yinaicheng.cache.redis.password=your_password
```

### 限流配置

```properties
# 限流基础配置
yinaicheng.limit.enabled=true
yinaicheng.limit.key-prefix=limit:
yinaicheng.limit.default-period=60
yinaicheng.limit.default-count=100
yinaicheng.limit.default-algorithm=fixed_window

# 限流Redis配置
yinaicheng.limit.redis.host=localhost
yinaicheng.limit.redis.port=6379
yinaicheng.limit.redis.database=1
yinaicheng.limit.redis.timeout=2000
yinaicheng.limit.redis.password=your_password
```

### 安全验签配置

```properties
# 安全验签基础配置
yinaicheng.security.enabled=true
yinaicheng.security.private-key=your_secret_key_here
yinaicheng.security.default-timestamp-validity-period=3000
yinaicheng.security.default-nonce-validity-period=3
yinaicheng.security.default-sign-algorithm=MD5
yinaicheng.security.enable-timestamp=true
yinaicheng.security.enable-nonce=true
yinaicheng.security.enable-sign=true

# 安全验签Redis配置
yinaicheng.security.redis.host=localhost
yinaicheng.security.redis.port=6379
yinaicheng.security.redis.database=2
yinaicheng.security.redis.timeout=2000
yinaicheng.security.redis.password=your_password
```

## 注解说明

### @CacheAnnotation

缓存注解，用于方法级缓存管理。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| operateCacheKeyPrefix | String | "" | 缓存key前缀 |
| operateCacheKey | String[] | {} | 缓存key规则，支持SpEL表达式 |
| judgeSpel | boolean | true | 是否使用SpEL表达式 |
| duration | int | 1 | 缓存时长（分钟） |
| cacheOperateType | CachedOperationTypeEnum | QUERY_CACHE | 缓存操作类型 |
| cacheName | String | "default" | 缓存名称 |
| cacheNull | boolean | false | 是否允许缓存空值 |
| condition | String | "" | 缓存条件，支持SpEL表达式 |
| unless | String | "" | 排除缓存条件，支持SpEL表达式 |
| sync | boolean | false | 缓存同步，防止缓存击穿 |

### @DistributedLimitTrafficAnnotation

分布式限流注解，用于接口访问频率控制。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| description | String | "" | 限流描述 |
| keyPrefix | String | "limit:" | key前缀 |
| key | String | "" | 限流key，支持SpEL表达式 |
| period | int | 60 | 时间窗口（秒） |
| count | int | 100 | 最大访问次数 |
| distributedLimitTrafficTypeEnum | DistributedLimitTrafficTypeEnum | CUSTOM_KEY | 限流类型 |
| message | String | "访问过于频繁，请稍后再试" | 限流提示消息 |
| enabled | boolean | true | 是否启用限流 |
| algorithm | String | "fixed_window" | 限流算法 |

### @SecurityVerificySignAnnotation

安全验签注解，用于接口调用的签名校验。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| enableTimestamp | boolean | true | 是否启用时间戳验证 |
| timestampValidityPeriod | long | 3000L | 时间戳有效期（毫秒） |
| enableNonce | boolean | true | 是否启用随机数验证 |
| nonceValidityPeriod | int | 3 | 随机数有效期（分钟） |
| signAlgorithm | String | "MD5" | 签名算法 |
| enableSign | boolean | true | 是否启用签名验证 |
| message | String | "签名验证失败" | 验证失败提示消息 |
| signField | String | "sign" | 签名字段名称 |
| timestampField | String | "timestamp" | 时间戳字段名称 |
| nonceField | String | "nonce" | 随机数字段名称 |
| enabled | boolean | true | 是否启用验签功能 |

## 客户端调用示例

### 安全验签客户端调用

```java
// 生成签名的示例代码
public class SignUtils {
    
    public static String generateSign(String nonce, String timestamp, String privateKey) {
        String data = "context=" + nonce + timestamp + "&" + privateKey;
        return MD5Utils.createSign(data);
    }
    
    public static void callSecureApi() {
        String nonce = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String privateKey = "your_secret_key";
        String sign = generateSign(nonce, timestamp, privateKey);
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("nonce", nonce);
        headers.set("timestamp", timestamp);
        headers.set("sign", sign);
        
        // 发送请求
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/secure", 
            HttpMethod.POST, 
            entity, 
            String.class
        );
    }
}
```

## 高级特性

### 1. SpEL表达式支持

在缓存key和限流key中，支持使用SpEL表达式：

```java
// 使用方法参数
@CacheAnnotation(operateCacheKey = {"#userId", "#status"})
public List<Order> getOrdersByUserAndStatus(Long userId, String status) {
    // ...
}

// 使用对象属性
@CacheAnnotation(operateCacheKey = {"#user.id", "#user.role"})
public User getUserInfo(User user) {
    // ...
}

// 使用条件表达式
@CacheAnnotation(
    condition = "#userId > 0",
    unless = "#result == null"
)
public User getUserById(Long userId) {
    // ...
}
```

### 2. 多级缓存

本 Starter 支持本地缓存 + Redis 的多级缓存架构：

- **一级缓存（本地）**：基于内存的高速缓存
- **二级缓存（Redis）**：分布式缓存，支持集群部署

### 3. 限流算法

支持两种限流算法：

- **fixed_window**：固定窗口算法，简单高效
- **sliding_window**：滑动窗口算法，更加精确

### 4. 缓存预热和清理

```java
// 批量预热缓存
@Component
public class CacheWarmUp {
    
    @Autowired
    private RedisTemplate<String, Object> cacheRedisTemplate;
    
    @PostConstruct
    public void warmUp() {
        // 预热热点数据
        List<User> hotUsers = userService.getHotUsers();
        for (User user : hotUsers) {
            String key = "user:" + user.getId();
            cacheRedisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
        }
    }
}

// 定时清理过期缓存
@Component
public class CacheCleanUp {
    
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanExpiredCache() {
        // 清理逻辑
    }
}
```

## 监控和统计

### 缓存命中率统计

```java
@Component
public class CacheMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public CacheMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hit")
            .tag("cache", cacheName)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.miss")
            .tag("cache", cacheName)
            .register(meterRegistry)
            .increment();
    }
}
```

## 故障排查

### 常见问题

1. **缓存不生效**
   - 检查是否启用了缓存功能：`yinaicheng.cache.enabled=true`
   - 检查Redis连接配置是否正确
   - 确认方法上是否正确使用了 `@CacheAnnotation` 注解

2. **限流不生效**
   - 检查是否启用了限流功能：`yinaicheng.limit.enabled=true`
   - 检查Redis连接配置是否正确
   - 确认方法上是否正确使用了 `@DistributedLimitTrafficAnnotation` 注解

3. **验签失败**
   - 检查客户端签名生成逻辑是否正确
   - 确认服务端密钥配置：`yinaicheng.security.private-key`
   - 检查时间戳是否在有效期内
   - 确认nonce没有重复使用

### 日志配置

```properties
# 开启详细日志
logging.level.top.yinaicheng=DEBUG
```

## 版本兼容性

- Spring Boot: 2.2.x ~ 2.4.x
- Java: 1.8+
- Redis: 3.0+

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个项目。

## 许可证

本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请联系：
- 作者：yinaicheng
- 邮箱：your-email@example.com
# Cache Yinaicheng Spring Boot Starter

一个基于 Spring Boot 的自研缓存与系统基础功能封装的 Starter，提供缓存管理、分布式限流、安全验签等核心功能。

## 功能特性

- 🚀 **系统缓存**：支持方法级缓存、自动失效、条件缓存等
- 🔒 **分布式限流**：支持基于 IP、自定义 Key 的限流策略
- 🛡️ **安全验签**：提供接口调用的签名校验机制
- ⚡ **高性能**：基于 Redis 和 J2Cache 的多级缓存架构
- 🎯 **易使用**：注解驱动，无侵入集成
- 🔧 **可配置**：丰富的配置选项，支持个性化定制

## 快速开始

### 1. 添加依赖

在您的项目 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>top.yinaicheng</groupId>
    <artifactId>cache-yinaicheng-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在 `application.properties` 或 `application.yml` 中添加配置：

```properties
# 启用功能
yinaicheng.cache.enabled=true
yinaicheng.limit.enabled=true
yinaicheng.security.enabled=true

# Redis 配置
yinaicheng.cache.redis.host=localhost
yinaicheng.cache.redis.port=6379
yinaicheng.cache.redis.database=0

# 安全密钥
yinaicheng.security.private-key=your_secret_key
```

### 3. 使用示例

#### 缓存功能

```java
@Service
public class UserService {
    
    // 查询缓存
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        duration = 30,
        cacheOperateType = CachedOperationTypeEnum.QUERY_CACHE
    )
    public User getUserById(Long userId) {
        // 从数据库查询用户
        return userRepository.findById(userId);
    }
    
    // 删除缓存
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        cacheOperateType = CachedOperationTypeEnum.DELETE_CACHE_BY_KEY
    )
    public void updateUser(Long userId, User user) {
        userRepository.update(user);
    }
}
```

#### 限流功能

```java
@RestController
public class ApiController {
    
    // IP 限流
    @DistributedLimitTrafficAnnotation(
        description = "API访问限流",
        period = 60,
        count = 100,
        distributedLimitTrafficTypeEnum = DistributedLimitTrafficTypeEnum.REQUESTER_IP
    )
    @GetMapping("/api/data")
    public Result getData() {
        return Result.success();
    }
    
    // 自定义Key限流
    @DistributedLimitTrafficAnnotation(
        description = "用户访问限流",
        key = "#userId",
        period = 60,
        count = 10,
        distributedLimitTrafficTypeEnum = DistributedLimitTrafficTypeEnum.CUSTOM_KEY
    )
    @GetMapping("/api/user/{userId}")
    public Result getUserInfo(@PathVariable Long userId) {
        return Result.success();
    }
}
```

#### 安全验签功能

```java
@RestController
public class SecureController {
    
    @SecurityVerificySignAnnotation(
        enableTimestamp = true,
        enableNonce = true,
        enableSign = true,
        message = "签名验证失败"
    )
    @PostMapping("/api/secure")
    public Result secureApi(@RequestBody String data) {
        return Result.success();
    }
}
```

## 详细配置

### 缓存配置

```properties
# 缓存基础配置
yinaicheng.cache.enabled=true
yinaicheng.cache.default-expiration=30
yinaicheng.cache.key-prefix=cache:
yinaicheng.cache.cache-null-values=false
yinaicheng.cache.max-capacity=10000

# 本地缓存配置
yinaicheng.cache.local.enabled=true
yinaicheng.cache.local.max-size=1000
yinaicheng.cache.local.expire-after-write=10

# Redis缓存配置
yinaicheng.cache.redis.enabled=true
yinaicheng.cache.redis.host=localhost
yinaicheng.cache.redis.port=6379
yinaicheng.cache.redis.database=0
yinaicheng.cache.redis.timeout=2000
yinaicheng.cache.redis.password=your_password
```

### 限流配置

```properties
# 限流基础配置
yinaicheng.limit.enabled=true
yinaicheng.limit.key-prefix=limit:
yinaicheng.limit.default-period=60
yinaicheng.limit.default-count=100
yinaicheng.limit.default-algorithm=fixed_window

# 限流Redis配置
yinaicheng.limit.redis.host=localhost
yinaicheng.limit.redis.port=6379
yinaicheng.limit.redis.database=1
yinaicheng.limit.redis.timeout=2000
yinaicheng.limit.redis.password=your_password
```

### 安全验签配置

```properties
# 安全验签基础配置
yinaicheng.security.enabled=true
yinaicheng.security.private-key=your_secret_key_here
yinaicheng.security.default-timestamp-validity-period=3000
yinaicheng.security.default-nonce-validity-period=3
yinaicheng.security.default-sign-algorithm=MD5
yinaicheng.security.enable-timestamp=true
yinaicheng.security.enable-nonce=true
yinaicheng.security.enable-sign=true

# 安全验签Redis配置
yinaicheng.security.redis.host=localhost
yinaicheng.security.redis.port=6379
yinaicheng.security.redis.database=2
yinaicheng.security.redis.timeout=2000
yinaicheng.security.redis.password=your_password
```

## 注解说明

### @CacheAnnotation

缓存注解，用于方法级缓存管理。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| operateCacheKeyPrefix | String | "" | 缓存key前缀 |
| operateCacheKey | String[] | {} | 缓存key规则，支持SpEL表达式 |
| judgeSpel | boolean | true | 是否使用SpEL表达式 |
| duration | int | 1 | 缓存时长（分钟） |
| cacheOperateType | CachedOperationTypeEnum | QUERY_CACHE | 缓存操作类型 |
| cacheName | String | "default" | 缓存名称 |
| cacheNull | boolean | false | 是否允许缓存空值 |
| condition | String | "" | 缓存条件，支持SpEL表达式 |
| unless | String | "" | 排除缓存条件，支持SpEL表达式 |
| sync | boolean | false | 缓存同步，防止缓存击穿 |

### @DistributedLimitTrafficAnnotation

分布式限流注解，用于接口访问频率控制。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| description | String | "" | 限流描述 |
| keyPrefix | String | "limit:" | key前缀 |
| key | String | "" | 限流key，支持SpEL表达式 |
| period | int | 60 | 时间窗口（秒） |
| count | int | 100 | 最大访问次数 |
| distributedLimitTrafficTypeEnum | DistributedLimitTrafficTypeEnum | CUSTOM_KEY | 限流类型 |
| message | String | "访问过于频繁，请稍后再试" | 限流提示消息 |
| enabled | boolean | true | 是否启用限流 |
| algorithm | String | "fixed_window" | 限流算法 |

### @SecurityVerificySignAnnotation

安全验签注解，用于接口调用的签名校验。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| enableTimestamp | boolean | true | 是否启用时间戳验证 |
| timestampValidityPeriod | long | 3000L | 时间戳有效期（毫秒） |
| enableNonce | boolean | true | 是否启用随机数验证 |
| nonceValidityPeriod | int | 3 | 随机数有效期（分钟） |
| signAlgorithm | String | "MD5" | 签名算法 |
| enableSign | boolean | true | 是否启用签名验证 |
| message | String | "签名验证失败" | 验证失败提示消息 |
| signField | String | "sign" | 签名字段名称 |
| timestampField | String | "timestamp" | 时间戳字段名称 |
| nonceField | String | "nonce" | 随机数字段名称 |
| enabled | boolean | true | 是否启用验签功能 |

## 客户端调用示例

### 安全验签客户端调用

```java
// 生成签名的示例代码
public class SignUtils {
    
    public static String generateSign(String nonce, String timestamp, String privateKey) {
        String data = "context=" + nonce + timestamp + "&" + privateKey;
        return MD5Utils.createSign(data);
    }
    
    public static void callSecureApi() {
        String nonce = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String privateKey = "your_secret_key";
        String sign = generateSign(nonce, timestamp, privateKey);
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("nonce", nonce);
        headers.set("timestamp", timestamp);
        headers.set("sign", sign);
        
        // 发送请求
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/secure", 
            HttpMethod.POST, 
            entity, 
            String.class
        );
    }
}
```

## 高级特性

### 1. SpEL表达式支持

在缓存key和限流key中，支持使用SpEL表达式：

```java
// 使用方法参数
@CacheAnnotation(operateCacheKey = {"#userId", "#status"})
public List<Order> getOrdersByUserAndStatus(Long userId, String status) {
    // ...
}

// 使用对象属性
@CacheAnnotation(operateCacheKey = {"#user.id", "#user.role"})
public User getUserInfo(User user) {
    // ...
}

// 使用条件表达式
@CacheAnnotation(
    condition = "#userId > 0",
    unless = "#result == null"
)
public User getUserById(Long userId) {
    // ...
}
```

### 2. 多级缓存

本 Starter 支持本地缓存 + Redis 的多级缓存架构：

- **一级缓存（本地）**：基于内存的高速缓存
- **二级缓存（Redis）**：分布式缓存，支持集群部署

### 3. 限流算法

支持两种限流算法：

- **fixed_window**：固定窗口算法，简单高效
- **sliding_window**：滑动窗口算法，更加精确

### 4. 缓存预热和清理

```java
// 批量预热缓存
@Component
public class CacheWarmUp {
    
    @Autowired
    private RedisTemplate<String, Object> cacheRedisTemplate;
    
    @PostConstruct
    public void warmUp() {
        // 预热热点数据
        List<User> hotUsers = userService.getHotUsers();
        for (User user : hotUsers) {
            String key = "user:" + user.getId();
            cacheRedisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
        }
    }
}

// 定时清理过期缓存
@Component
public class CacheCleanUp {
    
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanExpiredCache() {
        // 清理逻辑
    }
}
```

## 监控和统计

### 缓存命中率统计

```java
@Component
public class CacheMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public CacheMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hit")
            .tag("cache", cacheName)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.miss")
            .tag("cache", cacheName)
            .register(meterRegistry)
            .increment();
    }
}
```

## 故障排查

### 常见问题

1. **缓存不生效**
   - 检查是否启用了缓存功能：`yinaicheng.cache.enabled=true`
   - 检查Redis连接配置是否正确
   - 确认方法上是否正确使用了 `@CacheAnnotation` 注解

2. **限流不生效**
   - 检查是否启用了限流功能：`yinaicheng.limit.enabled=true`
   - 检查Redis连接配置是否正确
   - 确认方法上是否正确使用了 `@DistributedLimitTrafficAnnotation` 注解

3. **验签失败**
   - 检查客户端签名生成逻辑是否正确
   - 确认服务端密钥配置：`yinaicheng.security.private-key`
   - 检查时间戳是否在有效期内
   - 确认nonce没有重复使用

### 日志配置

```properties
# 开启详细日志
logging.level.top.yinaicheng=DEBUG
```

## 版本兼容性

- Spring Boot: 2.2.x ~ 2.4.x
- Java: 1.8+
- Redis: 3.0+

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个项目。

## 许可证

本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请联系：
- 作者：yinaicheng
- 邮箱：your-email@example.com
