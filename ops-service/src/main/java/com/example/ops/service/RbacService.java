package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RBAC 权限服务
 * 基于设计文档 docs/PERMISSION_MODEL.md
 */
@Service
public class RbacService {

    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userRoles = new ConcurrentHashMap<>();

    /**
     * 角色
     */
    public static class Role {
        private final String code;
        private final String name;
        private final Set<String> permissions;
        private String description;

        public Role(String code, String name, Set<String> permissions) {
            this.code = code;
            this.name = name;
            this.permissions = permissions;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public Set<String> getPermissions() { return permissions; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 初始化默认角色 (基于权限模型设计)
     */
    public void initDefaultRoles() {
        // ADMIN - 超级管理员 (全部权限)
        Set<String> adminPerms = new LinkedHashSet<>(Arrays.asList(
            // 监控
            "monitor:view", "monitor:metrics:query", "monitor:dashboard:edit",
            // 告警
            "alert:view", "alert:manage", "alert:acknowledge", "alert:resolve", "alert:configure",
            // 运维
            "ops:view", "ops:manage", "ops:script:execute", "ops:task:create", "ops:task:cancel",
            // 自愈
            "heal:view", "heal:manage", "heal:execute", "heal:configure",
            // 灰度发布
            "release:view", "release:manage", "release:create", "release:rollback",
            // 系统
            "system:user:manage", "system:role:manage", "system:config:view", "system:config:edit", "system:log:view"
        ));
        Role admin = new Role("ADMIN", "超级管理员", adminPerms);
        admin.setDescription("系统最高权限，拥有所有操作权限");
        addRole(admin);

        // OPS - 运维工程师
        Set<String> opsPerms = new LinkedHashSet<>(Arrays.asList(
            // 监控
            "monitor:view", "monitor:metrics:query", "monitor:dashboard:edit",
            // 告警
            "alert:view", "alert:manage", "alert:acknowledge", "alert:resolve",
            // 运维
            "ops:view", "ops:manage", "ops:script:execute", "ops:task:create", "ops:task:cancel",
            // 自愈
            "heal:view",
            // 灰度发布
            "release:view", "release:manage", "release:create", "release:rollback",
            // 系统
            "system:config:view", "system:log:view"
        ));
        Role ops = new Role("OPS", "运维工程师", opsPerms);
        ops.setDescription("日常运维管理权限");
        addRole(ops);

        // DEVELOPER - 开发人员
        Set<String> devPerms = new LinkedHashSet<>(Arrays.asList(
            // 监控
            "monitor:view", "monitor:metrics:query",
            // 告警
            "alert:view",
            // 运维
            "ops:view", "ops:script:execute",
            // 灰度发布
            "release:view",
            // 系统
            "system:log:view"
        ));
        Role developer = new Role("DEVELOPER", "开发人员", devPerms);
        developer.setDescription("开发调试权限");
        addRole(developer);

        // READONLY - 只读用户
        Set<String> readPerms = new LinkedHashSet<>(Arrays.asList(
            "monitor:view", "alert:view", "release:view"
        ));
        Role readonly = new Role("READONLY", "只读用户", readPerms);
        readonly.setDescription("仅查看权限");
        addRole(readonly);
    }

    /**
     * 添加角色
     */
    public void addRole(Role role) {
        roles.put(role.getCode(), role);
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
    public Role getRole(String code) {
        return roles.get(code);
    }

    /**
     * 删除角色
     */
    public boolean deleteRole(String code) {
        // 不能删除预定义角色
        if ("ADMIN".equals(code) || "OPS".equals(code) || "DEVELOPER".equals(code) || "READONLY".equals(code)) {
            return false;
        }
        return roles.remove(code) != null;
    }

    /**
     * 分配角色给用户
     */
    public void assignRole(String userId, String roleCode) {
        userRoles.computeIfAbsent(userId, k -> new HashSet<>()).add(roleCode);
    }

    /**
     * 移除用户角色
     */
    public void removeRole(String userId, String roleCode) {
        Set<String> userRoleSet = userRoles.get(userId);
        if (userRoleSet != null) {
            userRoleSet.remove(roleCode);
        }
    }

    /**
     * 获取用户所有角色
     */
    public Set<String> getUserRoles(String userId) {
        return userRoles.getOrDefault(userId, new HashSet<>());
    }

    /**
     * 获取用户所有权限
     */
    public Set<String> getUserPermissions(String userId) {
        Set<String> userRoleSet = userRoles.get(userId);
        if (userRoleSet == null) {
            return new HashSet<>();
        }

        Set<String> permissions = new HashSet<>();
        for (String roleCode : userRoleSet) {
            Role role = roles.get(roleCode);
            if (role != null) {
                permissions.addAll(role.getPermissions());
            }
        }
        return permissions;
    }

    /**
     * 检查用户权限
     */
    public boolean hasPermission(String userId, String permission) {
        return getUserPermissions(userId).contains(permission);
    }

    /**
     * 检查用户是否有任意一个权限
     */
    public boolean hasAnyPermission(String userId, String... permissions) {
        Set<String> userPerms = getUserPermissions(userId);
        for (String perm : permissions) {
            if (userPerms.contains(perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户是否有所有权限
     */
    public boolean hasAllPermissions(String userId, String... permissions) {
        Set<String> userPerms = getUserPermissions(userId);
        for (String perm : permissions) {
            if (!userPerms.contains(perm)) {
                return false;
            }
        }
        return true;
    }
}
