package com.example.ops.controller;

import com.example.ops.service.knowledge.AIService;
import com.example.ops.service.knowledge.KnowledgeBaseService;
import com.example.ops.service.knowledge.AIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Dashboard API - Unified management interface data
 */
@RestController
@RequestMapping("/ops/dashboard")
public class DashboardController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private AIService aiService;

    @Autowired
    private AIChatService aiChatService;

    /**
     * Get dashboard overview
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        
        // Knowledge base stats
        List<KnowledgeBaseService.KnowledgeBase> kbs = knowledgeBaseService.getAllKnowledgeBases();
        int totalDocs = 0;
        for (KnowledgeBaseService.KnowledgeBase kb : kbs) {
            totalDocs += knowledgeBaseService.getDocuments(kb.getId()).size();
        }
        
        result.put("knowledgeBases", kbs.size());
        result.put("totalDocuments", totalDocs);
        
        // AI status
        result.put("aiProvider", aiService.getProviderInfo());
        
        // System status
        result.put("systemStatus", "running");
        result.put("uptime", System.currentTimeMillis());
        
        return result;
    }

    /**
     * Quick actions
     */
    @GetMapping("/actions")
    public Map<String, Object> getQuickActions() {
        List<Map<String, String>> actions = new ArrayList<>();
        
        actions.add(Map.of("id", "create-kb", "name", "Create Knowledge Base", "icon", "book"));
        actions.add(Map.of("id", "import-doc", "name", "Import Document", "icon", "upload"));
        actions.add(Map.of("id", "ask-ai", "name", "Ask AI", "icon", "message"));
        actions.add(Map.of("id", "create-workflow", "name", "Create Workflow", "icon", "workflow"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("actions", actions);
        return result;
    }

    /**
     * Recent activity
     */
    @GetMapping("/activity")
    public Map<String, Object> getRecentActivity() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Knowledge base activities
        List<KnowledgeBaseService.KnowledgeBase> kbs = knowledgeBaseService.getAllKnowledgeBases();
        for (KnowledgeBaseService.KnowledgeBase kb : kbs) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "knowledge_base");
            activity.put("name", kb.getName());
            activity.put("timestamp", kb.getCreatedAt());
            activities.add(activity);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("activities", activities);
        result.put("total", activities.size());
        return result;
    }
}
