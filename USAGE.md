# 使用说明文档

## 快速集成指南

### 1. Maven 依赖配置

在您的项目 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>top.yinaicheng</groupId>
    <artifactId>cache-yinaicheng-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件设置

在 `application.properties` 中添加配置：

```properties
# 启用功能
yinaicheng.cache.enabled=true
yinaicheng.limit.enabled=true
yinaicheng.security.enabled=true

# Redis 基础配置
yinaicheng.cache.redis.host=localhost
yinaicheng.cache.redis.port=6379
yinaicheng.cache.redis.database=0

# 安全密钥
yinaicheng.security.private-key=your_secret_key
```

### 3. 代码示例

#### 缓存功能使用

```java
@Service
public class UserService {
    
    // 查询缓存
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        duration = 30
    )
    public User getUserById(Long userId) {
        // 业务逻辑
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

#### 限流功能使用

```java
@RestController
public class ApiController {
    
    // IP限流
    @DistributedLimitTrafficAnnotation(
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

#### 安全验签使用

```java
@RestController
public class SecureController {
    
    @SecurityVerificySignAnnotation
    @PostMapping("/api/secure")
    public Result secureApi(@RequestBody String data) {
        return Result.success();
    }
}
```

### 4. 客户端调用示例

```java
// 生成签名参数
String nonce = UUID.randomUUID().toString();
String timestamp = String.valueOf(System.currentTimeMillis());
String privateKey = "your_secret_key";

// 生成签名
Map<String, Object> signMap = new HashMap<>();
signMap.put("context", nonce + timestamp);
String data = getMapSignText(signMap) + privateKey;
String sign = MD5Utils.createSign(data);

// 设置请求头
headers.set("nonce", nonce);
headers.set("timestamp", timestamp);
headers.set("sign", sign);
```

### 5. 运行示例项目

1. 确保 Redis 服务运行在本地 6379 端口
2. 进入 example 目录
3. 运行 `mvn spring-boot:run`
4. 访问 http://localhost:8080/api/users/1 测试缓存功能

### 6. 测试接口

- **缓存测试**: `GET /api/users/{id}` - 第一次查询会从数据库获取并缓存，后续查询直接从缓存返回
- **限流测试**: `GET /api/users/list` - 快速连续访问会触发限流
- **验签测试**: `POST /api/users/create` - 需要在请求头中包含正确的签名信息

### 7. 常见问题

1. **缓存不生效**: 检查Redis连接和配置
2. **限流不生效**: 确认注解参数设置正确
3. **验签失败**: 检查签名生成逻辑和密钥配置

### 8. 高级配置

详细配置选项请参考 README.md 中的配置说明章节。

## 最佳实践

1. **缓存设计**: 合理设置缓存过期时间，避免缓存雪崩
2. **限流策略**: 根据业务场景选择合适的限流算法和参数
3. **安全验签**: 定期更换密钥，确保接口调用安全
4. **监控运维**: 建议添加相关监控指标，及时发现问题