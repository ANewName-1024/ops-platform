package com.example.ops.service.knowledge;

import com.example.ops.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI Chat Service
 * Multi-channel AI chat via Feishu/Dingtalk
 */
@Service
public class AIChatService {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Chat session
     */
    public static class ChatSession {
        private final String sessionId;
        private final String userId;
        private final String channel;
        private final List<ChatMessage> messages;
        private final long createdAt;

        public ChatSession(String userId, String channel) {
            this.sessionId = "session-" + System.currentTimeMillis();
            this.userId = userId;
            this.channel = channel;
            this.messages = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
        }

        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public String getChannel() { return channel; }
        public List<ChatMessage> getMessages() { return messages; }
        public long getCreatedAt() { return createdAt; }

        public void addMessage(ChatMessage msg) { messages.add(msg); }
    }

    /**
     * Chat message
     */
    public static class ChatMessage {
        private final String role; // user, assistant
        private final String content;
        private final long timestamp;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public long getTimestamp() { return timestamp; }
    }

    private final Map<String, ChatSession> sessions = new HashMap<>();

    /**
     * Create chat session
     */
    public ChatSession createSession(String userId, String channel) {
        ChatSession session = new ChatSession(userId, channel);
        sessions.put(session.getSessionId(), session);
        return session;
    }

    /**
     * Get session
     */
    public ChatSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Chat with AI
     */
    public Map<String, Object> chat(String userId, String channel, String message, String knowledgeBaseId) {
        // Get or create session
        String sessionKey = userId + "-" + channel;
        ChatSession session = sessions.get(sessionKey);
        if (session == null) {
            session = createSession(userId, channel);
            sessions.put(sessionKey, session);
        }

        // Add user message
        session.addMessage(new ChatMessage("user", message));

        // Search knowledge base
        List<KnowledgeBaseService.SearchResult> results = knowledgeBaseService.search(knowledgeBaseId, message, 3);

        // Generate response
        String response;
        if (results.isEmpty()) {
            response = "Sorry, I couldn't find relevant information. Please try a different question.";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Based on knowledge base:\n\n");
            for (int i = 0; i < results.size(); i++) {
                KnowledgeBaseService.SearchResult r = results.get(i);
                sb.append(i + 1).append(". ").append(r.getContent()).append("\n\n");
            }
            response = sb.toString();
        }

        // Add assistant message
        session.addMessage(new ChatMessage("assistant", response));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("sessionId", session.getSessionId());
        result.put("response", response);
        result.put("sources", results.size());
        return result;
    }

    /**
     * Send notification to user
     */
    public boolean notify(String userId, String channel, String message) {
        // Simple implementation - just return success
        // In production, integrate with actual Feishu/Dingtalk APIs
        return true;
    }

    /**
     * Get chat history
     */
    public List<ChatMessage> getHistory(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        return session != null ? session.getMessages() : new ArrayList<>();
    }
}
