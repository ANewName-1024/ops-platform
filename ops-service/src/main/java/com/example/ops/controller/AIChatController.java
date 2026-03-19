package com.example.ops.controller;

import com.example.ops.service.knowledge.AIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ops/chat")
public class AIChatController {

    @Autowired
    private AIChatService aiChatService;

    /**
     * Send message to AI chat
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String channel = request.getOrDefault("channel", "FEISHU");
        String message = request.get("message");
        String knowledgeBaseId = request.get("knowledgeBaseId");

        if (message == null || message.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Message is required");
            return result;
        }

        // If no knowledge base specified, use default
        if (knowledgeBaseId == null || knowledgeBaseId.isEmpty()) {
            knowledgeBaseId = "default";
        }

        Map<String, Object> result = aiChatService.chat(userId, channel, message, knowledgeBaseId);
        return result;
    }

    /**
     * Create new chat session
     */
    @PostMapping("/sessions")
    public Map<String, Object> createSession(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String channel = request.getOrDefault("channel", "FEISHU");

        AIChatService.ChatSession session = aiChatService.createSession(userId, channel);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("sessionId", session.getSessionId());
        result.put("userId", session.getUserId());
        result.put("channel", session.getChannel());
        return result;
    }

    /**
     * Get chat history
     */
    @GetMapping("/sessions/{sessionId}/history")
    public Map<String, Object> getHistory(@PathVariable("sessionId") String sessionId) {
        var messages = aiChatService.getHistory(sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("messages", messages);
        result.put("total", messages.size());
        return result;
    }

    /**
     * Send notification to user
     */
    @PostMapping("/notify")
    public Map<String, Object> notify(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String channel = request.getOrDefault("channel", "FEISHU");
        String message = request.get("message");

        boolean success = aiChatService.notify(userId, channel, message);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Notification sent" : "Failed to send notification");
        return result;
    }
}
