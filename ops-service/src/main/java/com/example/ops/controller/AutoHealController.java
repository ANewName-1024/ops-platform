package com.example.ops.controller;

import com.example.ops.service.AutoHealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自愈管理 REST API
 */
@RestController
@RequestMapping("/ops/heal")
public class AutoHealController {

    @Autowired
    private AutoHealService autoHealService;

    /**
     * 初始化默认策略
     */
    @PostMapping("/init")
    public Map<String, Object> initDefaultStrategies() {
        autoHealService.initDefaultStrategies();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Default strategies initialized");
        return result;
    }

    /**
     * 获取所有策略
     */
    @GetMapping("/strategies")
    public Map<String, Object> getStrategies() {
        List<AutoHealService.HealStrategy> strategies = autoHealService.getAllStrategies();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("strategies", strategies);
        return result;
    }

    /**
     * 添加策略
     */
    @PostMapping("/strategies")
    public Map<String, Object> addStrategy(@RequestBody Map<String, Object> request) {
        String alertType = (String) request.get("alertType");
        String name = (String) request.get("name");
        
        AutoHealService.HealStrategy strategy = new AutoHealService.HealStrategy(alertType, name, new java.util.ArrayList<>());
        autoHealService.addStrategy(strategy);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Strategy added");
        return result;
    }

    /**
     * 删除策略
     */
    @DeleteMapping("/strategies/{alertType}")
    public Map<String, Object> deleteStrategy(@PathVariable String alertType) {
        autoHealService.removeStrategy(alertType);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Strategy removed");
        return result;
    }

    /**
     * 执行修复
     */
    @PostMapping("/execute/{alertType}")
    public Map<String, Object> executeHeal(@PathVariable String alertType) {
        AutoHealService.HealResult result = autoHealService.executeHeal(alertType, new HashMap<>());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("executedActions", result.getExecutedActions());
        return response;
    }
}
