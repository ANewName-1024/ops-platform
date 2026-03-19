package com.example.ops.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控指标 REST API
 */
@RestController
@RequestMapping("/api/monitoring")
public class MetricsController {
    
    @Autowired
    private MetricsStorageService metricsStorageService;
    
    @Autowired
    private MetricsCollector[] collectors;
    
    /**
     * 手动触发采集
     */
    @PostMapping("/collect")
    public Map<String, Object> triggerCollect() {
        MetricsContext context = new MetricsContext();
        
        for (MetricsCollector collector : collectors) {
            collector.collect(context);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("metricsCount", context.getMetrics().size());
        result.put("metrics", context.getMetrics());
        
        return result;
    }
    
    /**
     * 获取指标列表
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (name != null && startTime != null && endTime != null) {
            List<MetricsStorageService.MetricsData> data = metricsStorageService.queryMetrics(name, startTime, endTime);
            result.put("data", data);
        } else {
            // 返回最新的指标值
            Map<String, Double> latestMetrics = new HashMap<>();
            for (MetricsCollector collector : collectors) {
                MetricsContext context = new MetricsContext();
                collector.collect(context);
                latestMetrics.putAll(context.getMetrics());
            }
            result.put("data", latestMetrics);
        }
        
        result.put("success", true);
        return result;
    }
    
    /**
     * 获取单个指标的最新值
     */
    @GetMapping("/metrics/{name}")
    public Map<String, Object> getMetric(@PathVariable String name) {
        Map<String, Object> result = new HashMap<>();
        Double value = metricsStorageService.getLatestMetric(name);
        
        result.put("success", true);
        result.put("metric", name);
        result.put("value", value);
        
        return result;
    }
    
    /**
     * 获取采集器列表
     */
    @GetMapping("/collectors")
    public Map<String, Object> getCollectors() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, String> collectorList = new HashMap<>();
        for (MetricsCollector collector : collectors) {
            collectorList.put(collector.getName(), collector.getClass().getSimpleName());
        }
        
        result.put("success", true);
        result.put("collectors", collectorList);
        
        return result;
    }
}
