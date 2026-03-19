package com.example.ops.controller;

import com.example.ops.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务 REST API
 */
@RestController
@RequestMapping("/ops/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 初始化默认渠道
     */
    @PostMapping("/channels/init")
    public Map<String, Object> initChannels() {
        notificationService.initDefaultChannels();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Default channels initialized");
        return result;
    }

    /**
     * 获取所有渠道
     */
    @GetMapping("/channels")
    public Map<String, Object> getChannels() {
        List<NotificationService.NotificationChannel> channels = notificationService.getAllChannels();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("channels", channels);
        result.put("total", channels.size());
        return result;
    }

    /**
     * 添加渠道
     */
    @PostMapping("/channels")
    public Map<String, Object> addChannel(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String type = (String) request.get("type");
        @SuppressWarnings("unchecked")
        Map<String, String> config = (Map<String, String>) request.get("config");

        NotificationService.NotificationChannel channel = new NotificationService.NotificationChannel(name, type, config);
        notificationService.addChannel(channel);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("channelId", channel.getId());
        return result;
    }

    /**
     * 删除渠道
     */
    @DeleteMapping("/channels/{id}")
    public Map<String, Object> deleteChannel(@PathVariable String id) {
        boolean success = notificationService.deleteChannel(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Channel deleted" : "Channel not found");
        return result;
    }

    /**
     * 发送通知
     */
    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody Map<String, String> request) {
        String channelId = request.get("channelId");
        String title = request.get("title");
        String content = request.get("content");

        NotificationService.SendResult result = notificationService.send(channelId, title, content);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        return response;
    }

    /**
     * 发送通知 (指定类型)
     */
    @PostMapping("/send/{type}")
    public Map<String, Object> sendByType(
            @PathVariable String type,
            @RequestBody Map<String, String> request) {

        String title = request.get("title");
        String content = request.get("content");

        // 查找对应类型的渠道
        List<NotificationService.NotificationChannel> channels = notificationService.getAllChannels();
        String channelId = null;
        for (NotificationService.NotificationChannel ch : channels) {
            if (ch.getType().equalsIgnoreCase(type)) {
                channelId = ch.getId();
                break;
            }
        }

        if (channelId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "No channel found for type: " + type);
            return result;
        }

        NotificationService.SendResult sendResult = notificationService.send(channelId, title, content);

        Map<String, Object> response = new HashMap<>();
        response.put("success", sendResult.isSuccess());
        response.put("message", sendResult.getMessage());
        return response;
    }
}
