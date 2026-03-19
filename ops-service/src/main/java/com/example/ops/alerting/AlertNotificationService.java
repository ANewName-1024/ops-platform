package com.example.ops.alerting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 告警通知服务
 */
@Service
public class AlertNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationService.class);
    
    @Value("${alert.notification.feishu.webhook:}")
    private String feishuWebhook;
    
    @Value("${alert.notification.dingtalk.webhook:}")
    private String dingtalkWebhook;
    
    @Value("${alert.notification.email.to:}")
    private String emailTo;
    
    /**
     * 发送告警通知
     */
    public void sendNotification(Alert alert) {
        // 飞书通知
        if (feishuWebhook != null && !feishuWebhook.isEmpty()) {
            sendToFeishu(alert);
        }
        
        // 钉钉通知
        if (dingtalkWebhook != null && !dingtalkWebhook.isEmpty()) {
            sendToDingTalk(alert);
        }
        
        // 邮件通知
        if (emailTo != null && !emailTo.isEmpty()) {
            sendToEmail(alert);
        }
    }
    
    /**
     * 发送飞书消息
     */
    private void sendToFeishu(Alert alert) {
        try {
            String message = buildFeishuMessage(alert);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(feishuWebhook))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("飞书通知发送结果: {}", response.statusCode());
        } catch (Exception e) {
            logger.error("飞书通知发送失败", e);
        }
    }
    
    /**
     * 构建飞书消息
     */
    private String buildFeishuMessage(Alert alert) {
        return String.format("""
            {
                "msg_type": "text",
                "content": {
                    "text": "[%s] %s\\n服务: %s\\n指标: %s\\n当前值: %.2f\\n阈值: %.2f"
                }
            }
            """, 
            alert.getSeverity(),
            alert.getMessage(),
            alert.getServiceName(),
            alert.getMetricName(),
            alert.getMetricValue(),
            alert.getThreshold()
        );
    }
    
    /**
     * 发送钉钉消息
     */
    private void sendToDingTalk(Alert alert) {
        try {
            String message = buildDingTalkMessage(alert);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(dingtalkWebhook))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("钉钉通知发送结果: {}", response.statusCode());
        } catch (Exception e) {
            logger.error("钉钉通知发送失败", e);
        }
    }
    
    /**
     * 构建钉钉消息
     */
    private String buildDingTalkMessage(Alert alert) {
        return String.format("""
            {
                "msgtype": "text",
                "text": {
                    "content": "[%s] %s\\n服务: %s\\n指标: %s\\n当前值: %.2f\\n阈值: %.2f"
                }
            }
            """, 
            alert.getSeverity(),
            alert.getMessage(),
            alert.getServiceName(),
            alert.getMetricName(),
            alert.getMetricValue(),
            alert.getThreshold()
        );
    }
    
    /**
     * 发送邮件
     */
    private void sendToEmail(Alert alert) {
        // TODO: 实现邮件发送
        logger.info("邮件通知: 告警 {}", alert.getAlertId());
    }
}
