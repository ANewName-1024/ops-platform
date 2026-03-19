package com.example.ops.alerting;

import java.util.Map;

/**
 * 告警规则
 */
public class AlertRule {
    
    private Long id;
    private String name;
    private AlertType alertType;
    private String metricName;
    private AlertOperator operator;
    private Double threshold;
    private Integer duration; // 持续时间(秒)
    private AlertSeverity severity;
    private boolean enabled = true;
    private Map<String, String> tags;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    
    public AlertOperator getOperator() { return operator; }
    public void setOperator(AlertOperator operator) { this.operator = operator; }
    
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public Map<String, String> getTags() { return tags; }
    public void setTags(Map<String, String> tags) { this.tags = tags; }
}
