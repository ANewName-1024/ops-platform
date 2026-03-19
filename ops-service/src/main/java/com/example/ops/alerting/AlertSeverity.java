package com.example.ops.alerting;

/**
 * 告警严重级别
 */
public enum AlertSeverity {
    INFO("信息", 1),
    WARNING("警告", 2),
    CRITICAL("严重", 3);
    
    private final String description;
    private final int priority;
    
    AlertSeverity(String description, int priority) {
        this.description = description;
        this.priority = priority;
    }
    
    public String getDescription() { return description; }
    public int getPriority() { return priority; }
}
