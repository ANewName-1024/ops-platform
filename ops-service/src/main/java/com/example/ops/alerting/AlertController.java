package com.example.ops.alerting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警管理 REST API
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private AlertRuleEngine ruleEngine;
    
    /**
     * 获取所有告警
     */
    @GetMapping
    public Map<String, Object> getAlerts() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("alerts", alertService.getAllAlerts());
        return result;
    }
    
    /**
     * 获取告警规则列表
     */
    @GetMapping("/rules")
    public Map<String, Object> getRules() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rules", ruleEngine.getAllRules());
        return result;
    }
    
    /**
     * 添加告警规则
     */
    @PostMapping("/rules")
    public Map<String, Object> addRule(@RequestBody AlertRule rule) {
        ruleEngine.addRule(rule);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "规则添加成功");
        return result;
    }
    
    /**
     * 删除告警规则
     */
    @DeleteMapping("/rules/{name}")
    public Map<String, Object> deleteRule(@PathVariable String name) {
        ruleEngine.removeRule(name);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "规则删除成功");
        return result;
    }
    
    /**
     * 确认告警
     */
    @PostMapping("/{alertId}/acknowledge")
    public Map<String, Object> acknowledge(
            @PathVariable String alertId,
            @RequestParam String user) {
        alertService.acknowledge(alertId, user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "告警已确认");
        return result;
    }
    
    /**
     * 解决告警
     */
    @PostMapping("/{alertId}/resolve")
    public Map<String, Object> resolve(@PathVariable String alertId) {
        alertService.resolve(alertId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "告警已解决");
        return result;
    }
}
