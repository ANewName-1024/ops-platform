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
        result.put("total", roles.size());
        return result;
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/roles/{code}")
    public Map<String, Object> getRole(@PathVariable String code) {
        RbacService.Role role = rbacService.getRole(code);
        Map<String, Object> result = new HashMap<>();
        if (role != null) {
            result.put("success", true);
            result.put("role", role);
            result.put("permissionCount", role.getPermissions().size());
        } else {
            result.put("success", false);
            result.put("message", "Role not found");
        }
        return result;
    }

    /**
     * 添加角色
     */
    @PostMapping("/roles")
    public Map<String, Object> addRole(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Set<String> permissions = new java.util.HashSet<>((List<String>) request.get("permissions"));
        
        RbacService.Role role = new RbacService.Role(code, name, permissions);
        rbacService.addRole(role);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Role added: " + code);
        return result;
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{code}")
    public Map<String, Object> deleteRole(@PathVariable String code) {
        boolean success = rbacService.deleteRole(code);
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "Role deleted: " + code);
        } else {
            result.put("success", false);
            result.put("message", "Cannot delete predefined role: " + code);
        }
        return result;
    }

    /**
     * 分配角色给用户
     */
    @PostMapping("/users/{userId}/roles")
    public Map<String, Object> assignRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        
        String roleCode = request.get("roleCode");
        rbacService.assignRole(userId, roleCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Role " + roleCode + " assigned to user " + userId);
        return result;
    }

    /**
     * 移除用户角色
     */
    @DeleteMapping("/users/{userId}/roles/{roleCode}")
    public Map<String, Object> removeUserRole(
            @PathVariable String userId,
            @PathVariable String roleCode) {
        
        rbacService.removeRole(userId, roleCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Role " + roleCode + " removed from user " + userId);
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
        result.put("userId", userId);
        result.put("roles", roles);
        result.put("total", roles.size());
        return result;
    }

    /**
     * 获取用户权限
     */
    @GetMapping("/users/{userId}/permissions")
    public Map<String, Object> getUserPermissions(@PathVariable String userId) {
        Set<String> permissions = rbacService.getUserPermissions(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("permissions", permissions);
        result.put("total", permissions.size());
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
        result.put("userId", userId);
        result.put("permission", permission);
        result.put("hasPermission", hasPermission);
        return result;
    }
}
