package com.example.ops.alerting;

import com.example.ops.monitoring.MetricsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警规则引擎
 */
@Service
public class AlertRuleEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertRuleEngine.class);
    
    // 告警规则存储
    private final Map<String, AlertRule> rules = new ConcurrentHashMap<>();
    
    /**
     * 评估指标是否触发告警
     */
    public List<Alert> evaluate(MetricsContext context) {
        List<Alert> alerts = new ArrayList<>();
        
        for (AlertRule rule : rules.values()) {
            if (!rule.isEnabled()) continue;
            
            Double metricValue = context.getMetrics().get(rule.getMetricName());
            if (metricValue == null) continue;
            
            if (evaluateRule(rule, metricValue)) {
                Alert alert = createAlert(rule, context, metricValue);
                alerts.add(alert);
                logger.warn("告警触发: {} = {} {}", rule.getMetricName(), metricValue, rule.getThreshold());
            }
        }
        
        return alerts;
    }
    
    /**
     * 评估单条规则
     */
    private boolean evaluateRule(AlertRule rule, Double value) {
        switch (rule.getOperator()) {
            case GT: return value > rule.getThreshold();
            case LT: return value < rule.getThreshold();
            case GTE: return value >= rule.getThreshold();
            case LTE: return value <= rule.getThreshold();
            case EQ: return Math.abs(value - rule.getThreshold()) < 0.001;
            case NE: return Math.abs(value - rule.getThreshold()) >= 0.001;
            default: return false;
        }
    }
    
    /**
     * 创建告警
     */
    private Alert createAlert(AlertRule rule, MetricsContext context, Double value) {
        Alert alert = new Alert();
        alert.setAlertType(rule.getAlertType());
        alert.setSeverity(rule.getSeverity());
        alert.setMetricName(rule.getMetricName());
        alert.setMetricValue(value);
        alert.setThreshold(rule.getThreshold());
        alert.setServiceName(context.getServiceName());
        alert.setMessage(String.format("%s 告警: %s = %.2f, 阈值: %.2f", 
            rule.getSeverity(), rule.getMetricName(), value, rule.getThreshold()));
        alert.setStatus(AlertStatus.PENDING);
        return alert;
    }
    
    /**
     * 添加规则
     */
    public void addRule(AlertRule rule) {
        rules.put(rule.getName(), rule);
    }
    
    /**
     * 删除规则
     */
    public void removeRule(String name) {
        rules.remove(name);
    }
    
    /**
     * 获取所有规则
     */
    public List<AlertRule> getAllRules() {
        return new ArrayList<>(rules.values());
    }
}
