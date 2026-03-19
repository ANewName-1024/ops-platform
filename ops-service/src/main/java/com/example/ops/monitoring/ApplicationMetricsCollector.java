package com.example.ops.monitoring;

import org.springframework.stereotype.Component;

/**
 * 应用指标采集器
 * 采集应用级别的指标 (保留接口，可扩展)
 */
@Component
public class ApplicationMetricsCollector implements MetricsCollector {
    
    @Override
    public void collect(MetricsContext context) {
        // Spring Boot 3.x 使用 Micrometer
        // 这里预留接口，后续可集成 Micrometer
        
        // 示例: 自定义业务指标
        // context.addMetric("app.business.order.count", getOrderCount());
        // context.addMetric("app.business.user.count", getUserCount());
    }
}
