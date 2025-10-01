package com.example.demo.service;

import com.example.demo.entity.User;
import org.springframework.stereotype.Service;
import top.yinaicheng.annotation.CacheAnnotation;
import top.yinaicheng.constant.CachedOperationTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务 - 演示缓存功能
 * @author yinaicheng
 */
@Service
public class UserService {

    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 根据ID获取用户 - 演示查询缓存
     */
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        duration = 30,
        cacheOperateType = CachedOperationTypeEnum.QUERY_CACHE,
        condition = "#userId > 0",
        unless = "#result == null",
        cacheName = "userCache"
    )
    public User getUserById(Long userId) {
        // 模拟数据库查询
        simulateSlowQuery();
        
        User user = new User();
        user.setId(userId);
        user.setName("用户" + userId);
        user.setEmail("user" + userId + "@example.com");
        user.setAge(20 + (int)(userId % 50));
        
        System.out.println("从数据库查询用户: " + userId);
        return user;
    }

    /**
     * 更新用户 - 演示缓存删除
     */
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        cacheOperateType = CachedOperationTypeEnum.DELETE_CACHE_BY_KEY
    )
    public User updateUser(Long userId, User user) {
        // 模拟数据库更新
        user.setId(userId);
        System.out.println("更新用户: " + userId);
        return user;
    }

    /**
     * 删除用户 - 演示缓存删除
     */
    @CacheAnnotation(
        operateCacheKeyPrefix = "user:",
        operateCacheKey = {"#userId"},
        cacheOperateType = CachedOperationTypeEnum.DELETE_CACHE_BY_KEY
    )
    public void deleteUser(Long userId) {
        // 模拟数据库删除
        System.out.println("删除用户: " + userId);
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        user.setId(idGenerator.getAndIncrement());
        System.out.println("创建用户: " + user.getId());
        return user;
    }

    /**
     * 获取所有用户 - 演示列表缓存
     */
    @CacheAnnotation(
        operateCacheKeyPrefix = "users:",
        operateCacheKey = {"all"},
        duration = 10,
        cacheOperateType = CachedOperationTypeEnum.QUERY_CACHE,
        cacheName = "userListCache"
    )
    public List<User> getAllUsers() {
        // 模拟数据库查询
        simulateSlowQuery();
        
        System.out.println("从数据库查询所有用户");
        return Arrays.asList(
            createTestUser(1L, "张三", "zhangsan@example.com", 25),
            createTestUser(2L, "李四", "lisi@example.com", 30),
            createTestUser(3L, "王五", "wangwu@example.com", 28)
        );
    }

    private User createTestUser(Long id, String name, String email, Integer age) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }

    /**
     * 模拟慢查询
     */
    private void simulateSlowQuery() {
        try {
            Thread.sleep(1000); // 模拟1秒的数据库查询时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}