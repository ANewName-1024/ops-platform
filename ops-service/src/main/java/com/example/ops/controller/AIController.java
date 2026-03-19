package com.example.ops.controller;

import com.example.ops.service.knowledge.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ops/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * Chat with AI
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String context = request.get("context");

        if (prompt == null || prompt.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Prompt is required");
            return result;
        }

        AIService.ChatResponse response = aiService.chat(prompt, context);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("content", response.getContent());
        result.put("model", response.getModel());
        result.put("tokens", response.getTokens());
        return result;
    }

    /**
     * Get AI provider info
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return aiService.getProviderInfo();
    }

    /**
     * Generate embedding
     */
    @PostMapping("/embedding")
    public Map<String, Object> embedding(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Text is required");
            return result;
        }

        var embedding = aiService.generateEmbedding(text);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("embedding", embedding);
        result.put("dimensions", embedding.size());
        return result;
    }
}
