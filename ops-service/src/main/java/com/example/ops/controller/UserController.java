package com.example.ops.controller;

import com.example.ops.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/ops/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping
    public Map<String, Object> getUsers() {
        Map<String, Object> result = new HashMap<>();
        List<UserService.User> users = userService.getAllUsers();
        result.put("users", users);
        result.put("total", users.size());
        return result;
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        UserService.User user = userService.getUserById(id);
        if (user != null) {
            result.put("user", user);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "用户不存在");
        }
        return result;
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Map<String, Object> createUser(@RequestBody UserService.User user) {
        Map<String, Object> result = new HashMap<>();
        UserService.User created = userService.createUser(user);
        result.put("user", created);
        result.put("success", true);
        result.put("message", "用户创建成功");
        return result;
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody UserService.User user) {
        Map<String, Object> result = new HashMap<>();
        UserService.User updated = userService.updateUser(id, user);
        if (updated != null) {
            result.put("user", updated);
            result.put("success", true);
            result.put("message", "用户更新成功");
        } else {
            result.put("success", false);
            result.put("message", "用户不存在");
        }
        return result;
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.deleteUser(id);
        result.put("success", success);
        result.put("message", success ? "用户删除成功" : "用户不存在");
        return result;
    }

    /**
     * 更新用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Map<String, Object> result = new HashMap<>();
        Boolean enabled = request.get("enabled");
        boolean success = userService.updateUserStatus(id, enabled != null ? enabled : true);
        result.put("success", success);
        result.put("message", success ? "状态更新成功" : "用户不存在");
        return result;
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    public Map<String, Object> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String newPassword = request.get("password");
        boolean success = userService.updatePassword(id, newPassword);
        result.put("success", success);
        result.put("message", success ? "密码修改成功" : "用户不存在");
        return result;
    }
}
