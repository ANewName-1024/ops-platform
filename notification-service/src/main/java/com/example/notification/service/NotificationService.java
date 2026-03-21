package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepo;
    
    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }
    
    /**
     * 获取通知列表
     */
    public Map<String, Object> getNotificationList(Long userId) {
        List<Notification> notifications;
        if (userId != null && userId > 0) {
            notifications = notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            notifications = notificationRepo.findAll();
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (Notification n : notifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", n.getId());
            item.put("userId", n.getUserId() != null ? n.getUserId() : 0L);
            item.put("title", n.getTitle() != null ? n.getTitle() : "");
            item.put("content", n.getContent() != null ? n.getContent() : "");
            item.put("type", n.getType() != null ? n.getType() : "INFO");
            item.put("channel", n.getChannel() != null ? n.getChannel() : "SYSTEM");
            item.put("isRead", n.getIsRead() != null ? n.getIsRead() : false);
            item.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : "");
            list.add(item);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", list);
        result.put("total", list.size());
        return result;
    }
    
    /**
     * 获取通知详情
     */
    public Map<String, Object> getNotificationDetail(Long id) {
        return notificationRepo.findById(id)
            .map(n -> {
                Map<String, Object> result = new HashMap<>();
                result.put("id", n.getId());
                result.put("userId", n.getUserId() != null ? n.getUserId() : 0L);
                result.put("title", n.getTitle() != null ? n.getTitle() : "");
                result.put("content", n.getContent() != null ? n.getContent() : "");
                result.put("type", n.getType() != null ? n.getType() : "INFO");
                result.put("channel", n.getChannel() != null ? n.getChannel() : "SYSTEM");
                result.put("isRead", n.getIsRead() != null ? n.getIsRead() : false);
                result.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : "");
                return result;
            })
            .orElseGet(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("error", "通知不存在");
                return result;
            });
    }
    
    /**
     * 发送通知
     */
    public Map<String, Object> sendNotification(Map<String, Object> params) {
        Notification n = new Notification();
        n.setTitle((String) params.get("title"));
        n.setContent((String) params.get("content"));
        n.setType((String) params.getOrDefault("type", "INFO"));
        n.setChannel((String) params.getOrDefault("channel", "SYSTEM"));
        
        if (params.containsKey("userId")) {
            n.setUserId(((Number) params.get("userId")).longValue());
        }
        
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        
        n = notificationRepo.save(n);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", n.getId());
        result.put("message", "通知发送成功");
        return result;
    }
    
    /**
     * 标记已读
     */
    public Map<String, Object> markAsRead(Long id) {
        return notificationRepo.findById(id)
            .map(n -> {
                n.setIsRead(true);
                notificationRepo.save(n);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "标记成功");
                return result;
            })
            .orElseGet(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "通知不存在");
                return result;
            });
    }
    
    /**
     * 全部标记已读
     */
    public Map<String, Object> markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepo.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        for (Notification n : notifications) {
            n.setIsRead(true);
            notificationRepo.save(n);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "全部标记成功");
        result.put("count", notifications.size());
        return result;
    }
    
    /**
     * 删除通知
     */
    public Map<String, Object> deleteNotification(Long id) {
        if (notificationRepo.existsById(id)) {
            notificationRepo.deleteById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");
            return result;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "通知不存在");
        return result;
    }
    
    /**
     * 获取未读数量
     */
    public Map<String, Object> getUnreadCount(Long userId) {
        long count = 0;
        if (userId != null && userId > 0) {
            count = notificationRepo.countByUserIdAndIsRead(userId, false);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", (int) count);
        return result;
    }
}
