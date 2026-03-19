package com.example.ops.service.knowledge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Service - Integration with LLM APIs
 * Supports OpenAI, Claude, local models
 */
@Service
public class AIService {

    @Value("${ai.provider:openai}")
    private String provider;

    @Value("${ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${ai.openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    @Value("${ai.claude.api-key:}")
    private String claudeApiKey;

    @Value("${ai.claude.model:claude-3-haiku-20240307}")
    private String claudeModel;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Chat completion request
     */
    public static class ChatRequest {
        private String model;
        private List<Map<String, String>> messages;
        private double temperature = 0.7;
        private int maxTokens = 1000;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public List<Map<String, String>> getMessages() { return messages; }
        public void setMessages(List<Map<String, String>> messages) { this.messages = messages; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }

    /**
     * Chat completion response
     */
    public static class ChatResponse {
        private String content;
        private String model;
        private int tokens;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getTokens() { return tokens; }
        public void setTokens(int tokens) { this.tokens = tokens; }
    }

    /**
     * Chat with AI
     */
    public ChatResponse chat(String prompt) {
        return chat(prompt, null);
    }

    /**
     * Chat with AI and context
     */
    public ChatResponse chat(String prompt, String context) {
        List<Map<String, String>> messages = new ArrayList<>();

        // Add context if provided
        if (context != null && !context.isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a helpful assistant. Use the following context to answer questions:\n\n" + context);
            messages.add(systemMsg);
        }

        // Add user message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        ChatRequest request = new ChatRequest();
        request.setMessages(messages);

        switch (provider.toLowerCase()) {
            case "openai":
                return chatWithOpenAI(request);
            case "claude":
                return chatWithClaude(request);
            case "local":
                return chatWithLocal(request);
            default:
                // Return mock response for demo
                return mockResponse(prompt);
        }
    }

    /**
     * Chat with OpenAI
     */
    private ChatResponse chatWithOpenAI(ChatRequest request) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return mockResponse(request.getMessages().get(request.getMessages().size() - 1).get("content"));
        }

        request.setModel(openaiModel);

        try {
            // Simplified - would need actual API call
            return mockResponse(request.getMessages().get(request.getMessages().size() - 1).get("content"));
        } catch (Exception e) {
            ChatResponse response = new ChatResponse();
            response.setContent("Error: " + e.getMessage());
            return response;
        }
    }

    /**
     * Chat with Claude
     */
    private ChatResponse chatWithClaude(ChatRequest request) {
        if (claudeApiKey == null || claudeApiKey.isEmpty()) {
            return mockResponse(request.getMessages().get(request.getMessages().size() - 1).get("content"));
        }

        request.setModel(claudeModel);

        try {
            return mockResponse(request.getMessages().get(request.getMessages().size() - 1).get("content"));
        } catch (Exception e) {
            ChatResponse response = new ChatResponse();
            response.setContent("Error: " + e.getMessage());
            return response;
        }
    }

    /**
     * Chat with local model
     */
    private ChatResponse chatWithLocal(ChatRequest request) {
        return mockResponse(request.getMessages().get(request.getMessages().size() - 1).get("content"));
    }

    /**
     * Mock response for demo
     */
    private ChatResponse mockResponse(String prompt) {
        ChatResponse response = new ChatResponse();
        
        // Simple rule-based responses for demo
        String lowerPrompt = prompt.toLowerCase();
        
        if (lowerPrompt.contains("hello") || lowerPrompt.contains("hi")) {
            response.setContent("Hello! How can I help you today?");
        } else if (lowerPrompt.contains("help")) {
            response.setContent("I'm here to help! You can ask me about:\n- System monitoring\n- Creating workflows\n- Managing knowledge bases\n- Alert configurations\n\nWhat would you like to know?");
        } else if (lowerPrompt.contains("who are you")) {
            response.setContent("I'm an AI assistant built on the OpenClaw Ops Platform. I can help you with IT operations, knowledge management, and automation tasks.");
        } else if (lowerPrompt.contains("capabilities") || lowerPrompt.contains("what can you do")) {
            response.setContent("I can help you with:\n\n1. **IT Operations** - Monitor systems, manage alerts, automate tasks\n2. **Knowledge Management** - Search and query your knowledge base\n3. **Workflow Automation** - Create and execute automated workflows\n4. **Troubleshooting** - Analyze issues and suggest solutions\n\nJust ask me anything!");
        } else {
            response.setContent("I understand your question: \"" + prompt.substring(0, Math.min(50, prompt.length())) + "...\"\n\nI'm connected to the AI service. In production, I would provide a detailed answer using the configured LLM (OpenAI/Claude/Local).");
        }
        
        response.setModel(provider + " (demo)");
        response.setTokens(prompt.length() / 4);
        
        return response;
    }

    /**
     * Generate embedding
     */
    public List<Float> generateEmbedding(String text) {
        // Return mock embedding for demo
        Random random = new Random(text.hashCode());
        List<Float> embedding = new ArrayList<>();
        for (int i = 0; i < 384; i++) {
            embedding.add(random.nextFloat());
        }
        return embedding;
    }

    /**
     * Check if AI is configured
     */
    public boolean isConfigured() {
        return (provider.equalsIgnoreCase("openai") && !openaiApiKey.isEmpty()) ||
               (provider.equalsIgnoreCase("claude") && !claudeApiKey.isEmpty()) ||
               provider.equalsIgnoreCase("local");
    }

    /**
     * Get provider info
     */
    public Map<String, Object> getProviderInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("provider", provider);
        info.put("configured", isConfigured());
        info.put("model", provider.equalsIgnoreCase("openai") ? openaiModel : 
                               provider.equalsIgnoreCase("claude") ? claudeModel : "local");
        return info;
    }
}
