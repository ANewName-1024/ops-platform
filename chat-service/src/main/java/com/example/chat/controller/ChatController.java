package com.example.chat.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.example.chat.service.LLMService;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final LLMService llmService;
    
    // 简单的内存会话存储
    private final Map<String, List<Map<String, String>>> sessionHistory = new HashMap<>();
    
    public ChatController(LLMService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "chat-service");
        result.put("port", 8093);
        result.put("llm", llmService.getConfig());
        return result;
    }

    @GetMapping("/sessions")
    public Map<String, Object> listSessions() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> sessions = new ArrayList<>();
        
        for (Map.Entry<String, List<Map<String, String>>> entry : sessionHistory.entrySet()) {
            Map<String, Object> session = new HashMap<>();
            session.put("id", entry.getKey());
            session.put("messageCount", entry.getValue().size());
            sessions.add(session);
        }
        
        result.put("sessions", sessions);
        result.put("total", sessions.size());
        return result;
    }

    @PostMapping("/sessions")
    public Map<String, Object> createSession(@RequestBody Map<String, Object> params) {
        String sessionId = UUID.randomUUID().toString();
        sessionHistory.put(sessionId, new ArrayList<>());
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", sessionId);
        result.put("status", "ACTIVE");
        result.put("createdAt", new Date());
        return result;
    }

    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> params) {
        String sessionId = (String) params.getOrDefault("sessionId", "default");
        String message = (String) params.get("content");
        
        if (message == null || message.isEmpty()) {
            return Map.of("error", "消息内容不能为空");
        }
        
        // 获取会话历史
        List<Map<String, String>> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        
        // 添加用户消息到历史
        history.add(Map.of("role", "user", "content", message));
        
        // 调用 LLM
        Map<String, Object> llmResponse = llmService.chat(message, history);
        
        String responseContent = (String) llmResponse.get("content");
        
        // 添加助手回复到历史
        history.add(Map.of("role", "assistant", "content", responseContent));
        
        // 限制历史长度
        if (history.size() > 20) {
            history = history.subList(history.size() - 20, history.size());
            sessionHistory.put(sessionId, history);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", UUID.randomUUID().toString());
        result.put("sessionId", sessionId);
        result.put("role", "assistant");
        result.put("content", responseContent);
        result.put("createdAt", new Date());
        result.put("llmEnabled", llmService.isEnabled());
        
        return result;
    }

    @GetMapping("/history/{sessionId}")
    public Map<String, Object> getHistory(@PathVariable String sessionId) {
        List<Map<String, String>> history = sessionHistory.getOrDefault(sessionId, new ArrayList<>());
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("messages", history);
        result.put("total", history.size());
        return result;
    }
    
    @DeleteMapping("/sessions/{sessionId}")
    public Map<String, Object> deleteSession(@PathVariable String sessionId) {
        sessionHistory.remove(sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "会话已删除");
        return result;
    }
}
