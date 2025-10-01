package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yinaicheng.annotation.DistributedLimitTrafficAnnotation;
import top.yinaicheng.annotation.SecurityVerificySignAnnotation;
import top.yinaicheng.constant.DistributedLimitTrafficTypeEnum;

/**
 * 用户控制器 - 演示缓存、限流、验签功能
 * @author yinaicheng
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息 - 演示缓存功能
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 更新用户信息 - 演示缓存清理功能
     */
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    /**
     * 获取用户列表 - 演示IP限流功能
     */
    @DistributedLimitTrafficAnnotation(
        description = "用户列表API限流",
        period = 60,
        count = 10,
        distributedLimitTrafficTypeEnum = DistributedLimitTrafficTypeEnum.REQUESTER_IP,
        message = "访问过于频繁，请稍后再试"
    )
    @GetMapping("/list")
    public java.util.List<User> getUserList() {
        return userService.getAllUsers();
    }

    /**
     * 获取用户详情 - 演示自定义Key限流功能
     */
    @DistributedLimitTrafficAnnotation(
        description = "用户详情API限流",
        key = "#userId",
        period = 60,
        count = 5,
        distributedLimitTrafficTypeEnum = DistributedLimitTrafficTypeEnum.CUSTOM_KEY,
        message = "该用户访问过于频繁"
    )
    @GetMapping("/detail/{userId}")
    public User getUserDetail(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    /**
     * 创建用户 - 演示安全验签功能
     */
    @SecurityVerificySignAnnotation(
        enableTimestamp = true,
        enableNonce = true,
        enableSign = true,
        message = "签名验证失败，请检查请求参数"
    )
    @PostMapping("/create")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    /**
     * 删除用户 - 演示综合功能（验签+限流+缓存清理）
     */
    @SecurityVerificySignAnnotation(enableSign = true)
    @DistributedLimitTrafficAnnotation(
        description = "删除用户API限流",
        key = "#userId",
        period = 300, // 5分钟
        count = 1,    // 只允许删除1次
        distributedLimitTrafficTypeEnum = DistributedLimitTrafficTypeEnum.CUSTOM_KEY
    )
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return "用户删除成功";
    }
}