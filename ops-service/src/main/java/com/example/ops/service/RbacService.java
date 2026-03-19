package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RBAC 权限服务
 */
@Service
public class RbacService {

    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userRoles = new ConcurrentHashMap<>();

    /**
     * 角色
     */
    public static class Role {
        private final String name;
        private final Set<String> permissions;
        private String description;

        public Role(String name, Set<String> permissions) {
            this.name = name;
            this.permissions = permissions;
        }

        public String getName() { return name; }
        public Set<String> getPermissions() { return permissions; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 初始化默认角色
     */
    public void initDefaultRoles() {
        // 管理员角色
        Set<String> adminPerms = new HashSet<>(Arrays.asList(
            "admin:manage", "ops:view", "ops:manage", "alert:view", "alert:manage",
            "release:view", "release:manage", "heal:view", "heal:manage"
        ));
        addRole(new Role("ADMIN", adminPerms));

        // 运维角色
        Set<String> opsPerms = new HashSet<>(Arrays.asList(
            "ops:view", "ops:manage", "alert:view", "alert:manage", "release:view"
        ));
        addRole(new Role("OPS", opsPerms));

        // 只读角色
        Set<String> readPerms = new HashSet<>(Arrays.asList(
            "ops:view", "alert:view", "release:view"
        ));
        addRole(new Role("READONLY", readPerms));
    }

    /**
     * 添加角色
     */
    public void addRole(Role role) {
        roles.put(role.getName(), role);
    }

    /**
     * 获取所有角色
     */
    public List<Role> getAllRoles() {
        return new ArrayList<>(roles.values());
    }

    /**
     * 获取角色
     */
    public Role getRole(String name) {
        return roles.get(name);
    }

    /**
     * 删除角色
     */
    public boolean deleteRole(String name) {
        return roles.remove(name) != null;
    }

    /**
     * 分配角色给用户
     */
    public void assignRole(String userId, String roleName) {
        userRoles.computeIfAbsent(userId, k -> new HashSet<>()).add(roleName);
    }

    /**
     * 移除用户角色
     */
    public void removeRole(String userId, String roleName) {
        Set<String> roles = userRoles.get(userId);
        if (roles != null) {
            roles.remove(roleName);
        }
    }

    /**
     * 获取用户角色
     */
    public Set<String> getUserRoles(String userId) {
        return userRoles.getOrDefault(userId, new HashSet<>());
    }

    /**
     * 检查用户权限
     */
    public boolean hasPermission(String userId, String permission) {
        Set<String> userRoleSet = userRoles.get(userId);
        if (userRoleSet == null) return false;

        for (String roleName : userRoleSet) {
            Role role = roles.get(roleName);
            if (role != null && role.getPermissions().contains(permission)) {
                return true;
            }
        }
        return false;
    }
}
