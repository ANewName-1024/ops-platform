package com.example.ops.monitoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控指标上下文
 * 用于收集和传递指标数据
 */
@Component
public class MetricsContext {
    
    private String serviceName;
    private String instanceId;
    private Map<String, Double> metrics = new HashMap<>();
    private Map<String, String> tags = new HashMap<>();
    private long timestamp = System.currentTimeMillis();
    
    public void addMetric(String name, double value) {
        metrics.put(name, value);
    }
    
    public void addMetric(String name, double value, Map<String, String> tags) {
        metrics.put(name, value);
        if (tags != null) {
            this.tags.putAll(tags);
        }
    }
    
    public void addTag(String key, String value) {
        tags.put(key, value);
    }
    
    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    public Map<String, Double> getMetrics() {
        return metrics;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
