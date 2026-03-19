package com.example.ops.controller;

import com.example.ops.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RBAC 权限 REST API
 */
@RestController
@RequestMapping("/ops/rbac")
public class RbacController {

    @Autowired
    private RbacService rbacService;

    /**
     * 初始化默认角色
     */
    @PostMapping("/init")
    public Map<String, Object> initDefaultRoles() {
        rbacService.initDefaultRoles();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Default roles initialized");
        return result;
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    public Map<String, Object> getRoles() {
        List<RbacService.Role> roles = rbacService.getAllRoles();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("roles", roles);
        return result;
    }

    /**
     * 添加角色
     */
    @PostMapping("/roles")
    public Map<String, Object> addRole(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Set<String> permissions = new java.util.HashSet<>((List<String>) request.get("permissions"));
        
        RbacService.Role role = new RbacService.Role(name, permissions);
        rbacService.addRole(role);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Role added");
        return result;
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{name}")
    public Map<String, Object> deleteRole(@PathVariable String name) {
        rbacService.deleteRole(name);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Role deleted");
        return result;
    }

    /**
     * 分配角色给用户
     */
    @PostMapping("/users/{userId}/roles")
    public Map<String, Object> assignRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        
        String roleName = request.get("roleName");
        rbacService.assignRole(userId, roleName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Role assigned to user");
        return result;
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/users/{userId}/roles")
    public Map<String, Object> getUserRoles(@PathVariable String userId) {
        Set<String> roles = rbacService.getUserRoles(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("roles", roles);
        return result;
    }

    /**
     * 检查用户权限
     */
    @GetMapping("/users/{userId}/check/{permission}")
    public Map<String, Object> checkPermission(
            @PathVariable String userId,
            @PathVariable String permission) {
        
        boolean hasPermission = rbacService.hasPermission(userId, permission);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("hasPermission", hasPermission);
        return result;
    }
}
