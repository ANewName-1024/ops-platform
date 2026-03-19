package com.example.ops.alerting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 告警升级服务
 */
@Service
public class AlertEscalationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertEscalationService.class);
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // 未确认告警
    private final ConcurrentHashMap<String, Long> unacknowledgedAlerts = new ConcurrentHashMap<>();
    
    // 升级超时时间 (分钟)
    private static final int ESCALATION_TIMEOUT_MINUTES = 5;
    
    /**
     * 处理告警
     */
    public void handleAlert(Alert alert) {
        if (alert.getStatus() == AlertStatus.PENDING) {
            // 记录未确认告警
            unacknowledgedAlerts.put(alert.getAlertId(), System.currentTimeMillis());
            
            // 启动升级检查
            scheduleEscalationCheck(alert);
        }
    }
    
    /**
     * 告警确认时移除
     */
    public void onAcknowledge(String alertId) {
        unacknowledgedAlerts.remove(alertId);
    }
    
    /**
     * 告警解决时移除
     */
    public void onResolved(String alertId) {
        unacknowledgedAlerts.remove(alertId);
    }
    
    /**
     * 计划升级检查
     */
    private void scheduleEscalationCheck(Alert alert) {
        scheduler.schedule(() -> {
            // 检查是否仍未确认
            if (unacknowledgedAlerts.containsKey(alert.getAlertId())) {
                // 执行升级
                escalate(alert);
            }
        }, ESCALATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * 升级告警
     */
    private void escalate(Alert alert) {
        logger.warn("告警升级: {} 未确认超过 {} 分钟", alert.getAlertId(), ESCALATION_TIMEOUT_MINUTES);
        
        // 提升告警级别
        if (alert.getSeverity() == AlertSeverity.WARNING) {
            alert.setSeverity(AlertSeverity.CRITICAL);
        }
        
        // 发送升级通知
        sendEscalationNotification(alert);
    }
    
    /**
     * 发送升级通知
     */
    private void sendEscalationNotification(Alert alert) {
        logger.warn("发送升级通知: {} 级别: {}", alert.getAlertId(), alert.getSeverity());
    }
}
