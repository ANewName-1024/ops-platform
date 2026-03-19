package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知服务
 * 支持飞书、钉钉、邮件、Webhook 等渠道
 */
@Service
public class NotificationService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Map<String, NotificationChannel> channels = new ConcurrentHashMap<>();

    /**
     * 通知渠道配置
     */
    public static class NotificationChannel {
        private final String id;
        private final String name;
        private final String type; // FEISHU, DINGTALK, EMAIL, WEBHOOK
        private final Map<String, String> config;
        private boolean enabled = true;

        public NotificationChannel(String name, String type, Map<String, String> config) {
            this.id = "ch-" + System.currentTimeMillis();
            this.name = name;
            this.type = type;
            this.config = config;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public Map<String, String> getConfig() { return config; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * 初始化默认渠道
     */
    public void initDefaultChannels() {
        // 飞书测试渠道
        Map<String, String> feishuConfig = new HashMap<>();
        feishuConfig.put("webhookUrl", "https://open.feishu.cn/open-apis/bot/v2/hook/xxx");
        addChannel(new NotificationChannel("飞书通知", "FEISHU", feishuConfig));

        // 钉钉测试渠道
        Map<String, String> dingtalkConfig = new HashMap<>();
        dingtalkConfig.put("webhookUrl", "https://oapi.dingtalk.com/robot/send?access_token=xxx");
        addChannel(new NotificationChannel("钉钉通知", "DINGTALK", dingtalkConfig));
    }

    /**
     * 添加渠道
     */
    public void addChannel(NotificationChannel channel) {
        channels.put(channel.getId(), channel);
    }

    /**
     * 获取所有渠道
     */
    public List<NotificationChannel> getAllChannels() {
        return new ArrayList<>(channels.values());
    }

    /**
     * 删除渠道
     */
    public boolean deleteChannel(String id) {
        return channels.remove(id) != null;
    }

    /**
     * 发送通知
     */
    public SendResult send(String channelId, String title, String content) {
        NotificationChannel channel = channels.get(channelId);
        if (channel == null || !channel.isEnabled()) {
            return new SendResult(false, "Channel not found or disabled");
        }

        try {
            switch (channel.getType()) {
                case "FEISHU":
                    return sendFeishu(channel.getConfig().get("webhookUrl"), title, content);
                case "DINGTALK":
                    return sendDingtalk(channel.getConfig().get("webhookUrl"), title, content);
                case "WEBHOOK":
                    return sendWebhook(channel.getConfig().get("url"), title, content);
                default:
                    return new SendResult(false, "Unknown channel type: " + channel.getType());
            }
        } catch (Exception e) {
            return new SendResult(false, e.getMessage());
        }
    }

    /**
     * 发送飞书消息
     */
    private SendResult sendFeishu(String webhookUrl, String title, String content) {
        if (webhookUrl == null || webhookUrl.contains("xxx")) {
            return new SendResult(true, "Feishu webhook not configured (simulated)");
        }

        String json = String.format("""
            {
                "msg_type": "interactive",
                "card": {
                    "header": {
                        "title": {"tag": "plain_text", "content": "%s"},
                        "template": "blue"
                    },
                    "elements": [
                        {
                            "tag": "markdown",
                            "content": "%s"
                        }
                    ]
                }
            }
            """, title, content);

        return sendHttpRequest(webhookUrl, json);
    }

    /**
     * 发送钉钉消息
     */
    private SendResult sendDingtalk(String webhookUrl, String title, String content) {
        if (webhookUrl == null || webhookUrl.contains("xxx")) {
            return new SendResult(true, "Dingtalk webhook not configured (simulated)");
        }

        String json = String.format("""
            {
                "msgtype": "markdown",
                "markdown": {
                    "title": "%s",
                    "text": "## %s\\n\\n%s"
                }
            }
            """, title, title, content);

        return sendHttpRequest(webhookUrl, json);
    }

    /**
     * 发送 Webhook
     */
    private SendResult sendWebhook(String url, String title, String content) {
        if (url == null) {
            return new SendResult(false, "Webhook URL not configured");
        }

        String json = String.format("""
            {
                "title": "%s",
                "content": "%s",
                "timestamp": %d
            }
            """, title, content, System.currentTimeMillis());

        return sendHttpRequest(url, json);
    }

    /**
     * HTTP 请求
     */
    private SendResult sendHttpRequest(String url, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new SendResult(true, "Message sent successfully");
            } else {
                return new SendResult(false, "HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            return new SendResult(false, e.getMessage());
        }
    }

    /**
     * 发送结果
     */
    public static class SendResult {
        private final boolean success;
        private final String message;

        public SendResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
