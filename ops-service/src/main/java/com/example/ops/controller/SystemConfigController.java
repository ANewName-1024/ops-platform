package com.example.ops.controller;

import com.example.ops.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/ops/system")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("config", systemConfigService.getAllConfig());
        result.put("success", true);
        return result;
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/config")
    public Map<String, Object> updateConfig(@RequestBody Map<String, String> config) {
        Map<String, Object> result = new HashMap<>();
        systemConfigService.setConfig(config);
        result.put("success", true);
        result.put("message", "配置更新成功");
        return result;
    }

    /**
     * 获取单个配置
     */
    @GetMapping("/config/{key}")
    public Map<String, Object> getConfigByKey(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("value", systemConfigService.getConfig(key));
        result.put("success", true);
        return result;
    }

    /**
     * 设置单个配置
     */
    @PostMapping("/config/{key}")
    public Map<String, Object> setConfigByKey(@PathVariable String key, @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String value = request.get("value");
        systemConfigService.setConfig(key, value);
        result.put("success", true);
        result.put("message", "配置更新成功");
        return result;
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("info", systemConfigService.getSystemInfo());
        result.put("success", true);
        return result;
    }
}
