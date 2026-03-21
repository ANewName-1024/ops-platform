package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 */
@Service
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserService() {
        // 初始化默认用户
        users.put(1L, new User(1L, "admin", "admin@example.com", "管理员", "ADMIN", true));
        users.put(2L, new User(2L, "user01", "user01@example.com", "普通用户", "USER", true));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public User createUser(User user) {
        Long id = idGenerator.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    public User updateUser(Long id, User user) {
        if (!users.containsKey(id)) {
            return null;
        }
        user.setId(id);
        users.put(id, user);
        return user;
    }

    public boolean deleteUser(Long id) {
        return users.remove(id) != null;
    }

    public boolean updateUserStatus(Long id, boolean enabled) {
        User user = users.get(id);
        if (user != null) {
            user.setEnabled(enabled);
            return true;
        }
        return false;
    }

    public boolean updatePassword(Long id, String newPassword) {
        User user = users.get(id);
        if (user != null) {
            user.setPassword(newPassword);
            return true;
        }
        return false;
    }

    public static class User {
        private Long id;
        private String username;
        private String password;
        private String email;
        private String nickname;
        private String role;
        private boolean enabled;
        private String createdAt;

        public User() {}

        public User(Long id, String username, String email, String nickname, String role, boolean enabled) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.nickname = nickname;
            this.role = role;
            this.enabled = enabled;
            this.createdAt = new Date().toString();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
