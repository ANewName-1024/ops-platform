package com.example.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class LLMService {

    @Value("${llm.provider:openai}")
    private String provider;
    
    @Value("${llm.api.key:}")
    private String apiKey;
    
    @Value("${llm.api.endpoint:https://api.openai.com/v1}")
    private String endpoint;
    
    @Value("${llm.model:gpt-3.5-turbo}")
    private String model;
    
    @Value("${llm.temperature:0.7}")
    private double temperature;
    
    @Value("${llm.max_tokens:2000}")
    private int maxTokens;
    
    @Value("${llm.enabled:false}")
    private boolean enabled;
    
    private final RestTemplate restTemplate;
    
    public LLMService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 发送聊天请求到 LLM
     */
    public Map<String, Object> chat(String message, List<Map<String, String>> history) {
        if (!enabled) {
            return getMockResponse(message);
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "你是一个专业的运维助手，帮助用户解决技术问题。"));
            
            // 添加历史记录
            if (history != null) {
                messages.addAll(history);
            }
            
            // 添加当前消息
            messages.add(Map.of("role", "user", "content", message));
            
            requestBody.put("messages", messages);
            
            // 调用 LLM API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = endpoint + "/chat/completions";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map choice = (Map) choices.get(0);
                    Map messageResult = (Map) choice.get("message");
                    return Map.of(
                        "content", messageResult.get("content"),
                        "role", "assistant"
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("LLM API 调用失败: " + e.getMessage());
        }
        
        // 如果失败，返回模拟响应
        return getMockResponse(message);
    }
    
    /**
     * 获取模拟响应（LLM 未启用时）
     */
    private Map<String, Object> getMockResponse(String message) {
        String response;
        
        message = message.toLowerCase();
        
        if (message.contains("部署") || message.contains("deploy")) {
            response = "关于部署，我可以帮你：\n1. 前端部署 - 使用 nginx\n2. 后端部署 - 使用 JAR 包\n3. Docker 部署 - 一键启动";
        } else if (message.contains("监控") || message.contains("monitor")) {
            response = "监控系统可以监控：\n1. 服务器 CPU/内存/磁盘\n2. 应用性能指标\n3. 日志分析";
        } else if (message.contains("日志") || message.contains("log")) {
            response = "日志服务提供：\n1. 实时日志查看\n2. 日志搜索和过滤\n3. 日志导出功能";
        } else if (message.contains("告警") || message.contains("alert")) {
            response = "告警功能支持：\n1. CPU/内存告警\n2. 磁盘空间告警\n3. 服务down机告警\n4. 多种通知渠道";
        } else if (message.contains("你好") || message.contains("hello") || message.contains("hi")) {
            response = "你好！我是 AI 运维助手，可以帮助你解答关于系统运维的各种问题。有什么可以帮你的吗？";
        } else {
            response = "收到你的消息: " + message + "\n\n我可以帮助你解决：\n- 部署相关问题\n- 监控和告警配置\n- 日志分析\n- 性能优化\n- 故障排查\n\n请告诉我具体需求~";
        }
        
        return Map.of(
            "content", response,
            "role", "assistant"
        );
    }
    
    /**
     * 检查 LLM 是否可用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取 LLM 配置信息
     */
    public Map<String, Object> getConfig() {
        return Map.of(
            "enabled", enabled,
            "provider", provider,
            "model", model,
            "endpoint", endpoint
        );
    }
}
