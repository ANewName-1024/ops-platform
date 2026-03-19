package com.example.ops.monitoring;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prometheus 指标导出器
 * 将采集的指标导出到 Prometheus
 */
@Component
public class PrometheusExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(PrometheusExporter.class);
    
    private final CollectorRegistry registry = new CollectorRegistry();
    private final Map<String, Gauge> gaugeCache = new ConcurrentHashMap<>();
    
    /**
     * 导出指标到 Prometheus
     */
    public void export(MetricsContext context) {
        for (Map.Entry<String, Double> entry : context.getMetrics().entrySet()) {
            String metricName = sanitizeMetricName(entry.getKey());
            double value = entry.getValue();
            
            try {
                Gauge gauge = gaugeCache.computeIfAbsent(metricName, this::createGauge);
                gauge.labels(getLabels(context.getTags()))
                    .set(value);
            } catch (Exception e) {
                logger.warn("导出指标失败: {}", entry.getKey(), e);
            }
        }
    }
    
    /**
     * 创建 Gauge 指标
     */
    private Gauge createGauge(String name) {
        Builder builder = Gauge.build()
            .name(name)
            .help("Metric " + name);
        
        return builder.register(registry);
    }
    
    /**
     * 清理指标名称，替换非法字符
     */
    private String sanitizeMetricName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_:]", "_");
    }
    
    /**
     * 获取标签
     */
    private String[] getLabels(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        return tags.values().toArray(new String[0]);
    }
    
    /**
     * 获取 Prometheus Registry
     */
    public CollectorRegistry getRegistry() {
        return registry;
    }
}
