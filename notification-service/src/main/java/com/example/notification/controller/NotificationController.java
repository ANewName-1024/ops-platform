package com.example.notification.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    // 内存存储模拟
    private final List<Map<String, Object>> notifications = new ArrayList<>();
    private long idCounter = 1;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "notification-service");
        result.put("port", 8094);
        return result;
    }

    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", notifications);
        result.put("total", notifications.size());
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> params) {
        Map<String, Object> n = new HashMap<>();
        n.put("id", idCounter++);
        n.put("title", params.get("title"));
        n.put("content", params.get("content"));
        n.put("type", params.getOrDefault("type", "INFO"));
        n.put("channel", params.getOrDefault("channel", "SYSTEM"));
        n.put("isRead", false);
        n.put("createdAt", new Date().toString());
        notifications.add(n);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", n.get("id"));
        result.put("message", "通知发送成功");
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        for (Map<String, Object> n : notifications) {
            if (n.get("id").equals(id)) return n;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("error", "通知不存在");
        return result;
    }

    @PutMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id) {
        for (Map<String, Object> n : notifications) {
            if (n.get("id").equals(id)) {
                n.put("isRead", true);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "标记成功");
                return result;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "通知不存在");
        return result;
    }

    @PutMapping("/read-all")
    public Map<String, Object> markAllRead() {
        int count = 0;
        for (Map<String, Object> n : notifications) {
            if (n.get("isRead") == null || !(Boolean) n.get("isRead")) {
                n.put("isRead", true);
                count++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "全部标记成功");
        result.put("count", count);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).get("id").equals(id)) {
                notifications.remove(i);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "删除成功");
                return result;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "通知不存在");
        return result;
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount() {
        int count = 0;
        for (Map<String, Object> n : notifications) {
            if (n.get("isRead") == null || !(Boolean) n.get("isRead")) count++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return result;
    }
}
