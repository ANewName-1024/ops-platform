package com.example.ops.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标存储服务
 * 负责收集、存储和查询指标数据
 */
@Service
public class MetricsStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsStorageService.class);
    
    @Autowired
    private List<MetricsCollector> collectors;
    
    @Value("${spring.application.name:unknown}")
    private String serviceName;
    
    // 内存存储 (生产环境应使用 Prometheus/InfluxDB)
    private final Map<String, List<MetricsData>> metricsCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 10000;
    
    /**
     * 采集指标
     */
    @Scheduled(fixedRate = 10000) // 每10秒采集一次
    public void collectMetrics() {
        MetricsContext context = new MetricsContext();
        context.setServiceName(serviceName);
        context.setTimestamp(System.currentTimeMillis());
        
        for (MetricsCollector collector : collectors) {
            try {
                collector.collect(context);
            } catch (Exception e) {
                logger.error("采集指标失败: {}", collector.getName(), e);
            }
        }
        
        // 存储指标
        storeMetrics(context);
        
        logger.debug("采集指标完成: {} 个指标", context.getMetrics().size());
    }
    
    /**
     * 存储指标
     */
    private void storeMetrics(MetricsContext context) {
        for (Map.Entry<String, Double> entry : context.getMetrics().entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            
            MetricsData data = new MetricsData();
            data.setMetricName(key);
            data.setValue(value);
            data.setServiceName(context.getServiceName());
            data.setTimestamp(context.getTimestamp());
            data.setTags(context.getTags());
            
            metricsCache.computeIfAbsent(key, k -> new ArrayList<>()).add(data);
            
            // 限制缓存大小
            List<MetricsData> list = metricsCache.get(key);
            if (list.size() > MAX_CACHE_SIZE) {
                list.remove(0);
            }
        }
    }
    
    /**
     * 查询指标
     */
    public List<MetricsData> queryMetrics(String metricName, long startTime, long endTime) {
        List<MetricsData> result = new ArrayList<>();
        List<MetricsData> list = metricsCache.get(metricName);
        
        if (list != null) {
            for (MetricsData data : list) {
                if (data.getTimestamp() >= startTime && data.getTimestamp() <= endTime) {
                    result.add(data);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取最新的指标值
     */
    public Double getLatestMetric(String metricName) {
        List<MetricsData> list = metricsCache.get(metricName);
        if (list != null && !list.isEmpty()) {
            return list.get(list.size() - 1).getValue();
        }
        return null;
    }
    
    /**
     * 指标数据类
     */
    public static class MetricsData {
        private String serviceName;
        private String metricName;
        private Double value;
        private Long timestamp;
        private Map<String, String> tags;
        
        // Getters and Setters
        public String getServiceName() {
            return serviceName;
        }
        
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public String getMetricName() {
            return metricName;
        }
        
        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }
        
        public Double getValue() {
            return value;
        }
        
        public void setValue(Double value) {
            this.value = value;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
        
        public Map<String, String> getTags() {
            return tags;
        }
        
        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }
    }
}
