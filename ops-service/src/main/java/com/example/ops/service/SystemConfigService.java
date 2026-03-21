package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置服务
 */
@Service
public class SystemConfigService {

    private final Map<String, String> config = new ConcurrentHashMap<>();

    public SystemConfigService() {
        // 初始化默认配置
        config.put("system.name", "OPS 平台");
        config.put("system.description", "运维管理一体化平台");
        config.put("system.timezone", "Asia/Shanghai");
        config.put("system.language", "zh-CN");
        
        // 邮件配置
        config.put("mail.host", "smtp.example.com");
        config.put("mail.port", "587");
        config.put("mail.username", "");
        config.put("mail.password", "");
        config.put("mail.enabled", "false");
        
        // 安全配置
        config.put("security.password.strict", "true");
        config.put("security.password.expiry.days", "90");
        config.put("security.login.max.attempts", "5");
        config.put("security.login.lock.minutes", "30");
        config.put("security.twofactor.enabled", "false");
    }

    public Map<String, String> getAllConfig() {
        return new HashMap<>(config);
    }

    public String getConfig(String key) {
        return config.get(key);
    }

    public void setConfig(String key, String value) {
        config.put(key, value);
    }

    public void setConfig(Map<String, String> newConfig) {
        config.putAll(newConfig);
    }

    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("java.version", System.getProperty("java.version"));
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.arch", System.getProperty("os.arch"));
        info.put("os.version", System.getProperty("os.version"));
        info.put("os.arch", System.getProperty("os.arch"));
        info.put("user.name", System.getProperty("user.name"));
        info.put("user.dir", System.getProperty("user.dir"));
        info.put("java.home", System.getProperty("java.home"));
        info.put("runtime.available Processors", Runtime.getRuntime().availableProcessors());
        info.put("runtime.total.memory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB");
        info.put("runtime.free.memory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + " MB");
        info.put("runtime.max.memory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        info.put("start.time", new Date().toString());
        return info;
    }
}
