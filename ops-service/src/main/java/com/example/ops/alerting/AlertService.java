package com.example.ops.alerting;

import com.example.ops.monitoring.MetricsCollector;
import com.example.ops.monitoring.MetricsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警服务
 */
@Service
public class AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    @Autowired
    private AlertRuleEngine ruleEngine;
    
    @Autowired
    private MetricsCollector[] collectors;
    
    // 告警存储
    private final Map<String, Alert> alerts = new ConcurrentHashMap<>();
    
    /**
     * 定时检查告警
     */
    @Scheduled(fixedRate = 10000) // 每10秒检查一次
    public void checkAlerts() {
        MetricsContext context = new MetricsContext();
        
        // 采集指标
        for (MetricsCollector collector : collectors) {
            collector.collect(context);
        }
        
        // 评估告警规则
        List<Alert> triggeredAlerts = ruleEngine.evaluate(context);
        
        // 处理告警
        for (Alert alert : triggeredAlerts) {
            handleAlert(alert);
        }
    }
    
    /**
     * 处理告警
     */
    private void handleAlert(Alert alert) {
        // 生成告警ID
        String alertId = UUID.randomUUID().toString();
        alert.setAlertId(alertId);
        
        // 存储告警
        alerts.put(alertId, alert);
        
        // 发送通知
        sendNotification(alert);
        
        logger.warn("告警触发: {} - {}", alert.getAlertId(), alert.getMessage());
    }
    
    /**
     * 发送告警通知
     */
    private void sendNotification(Alert alert) {
        // TODO: 集成飞书/钉钉/邮件通知
        logger.info("发送告警通知: {} 严重级别: {}", alert.getMessage(), alert.getSeverity());
    }
    
    /**
     * 获取所有告警
     */
    public Map<String, Alert> getAllAlerts() {
        return new HashMap<>(alerts);
    }
    
    /**
     * 确认告警
     */
    public void acknowledge(String alertId, String user) {
        Alert alert = alerts.get(alertId);
        if (alert != null) {
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAssignedTo(user);
        }
    }
    
    /**
     * 解决告警
     */
    public void resolve(String alertId) {
        Alert alert = alerts.get(alertId);
        if (alert != null) {
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(System.currentTimeMillis());
        }
    }
}
