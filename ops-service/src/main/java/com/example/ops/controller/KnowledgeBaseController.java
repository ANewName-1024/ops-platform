package com.example.ops.controller;

import com.example.ops.service.knowledge.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ops/knowledge")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/bases")
    public Map<String, Object> createKnowledgeBase(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.getOrDefault("description", "");
        
        KnowledgeBaseService.KnowledgeBase kb = knowledgeBaseService.createKnowledgeBase(name, description);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", kb.getId());
        result.put("name", kb.getName());
        return result;
    }

    @GetMapping("/bases")
    public Map<String, Object> getKnowledgeBases() {
        List<KnowledgeBaseService.KnowledgeBase> bases = knowledgeBaseService.getAllKnowledgeBases();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("bases", bases);
        result.put("total", bases.size());
        return result;
    }

    @GetMapping("/bases/{id}")
    public Map<String, Object> getKnowledgeBase(@PathVariable String id) {
        KnowledgeBaseService.KnowledgeBase kb = knowledgeBaseService.getKnowledgeBase(id);
        
        Map<String, Object> result = new HashMap<>();
        if (kb != null) {
            result.put("success", true);
            result.put("knowledgeBase", kb);
        } else {
            result.put("success", false);
            result.put("message", "Knowledge base not found");
        }
        return result;
    }

    @DeleteMapping("/bases/{id}")
    public Map<String, Object> deleteKnowledgeBase(@PathVariable String id) {
        boolean success = knowledgeBaseService.deleteKnowledgeBase(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Deleted" : "Not found");
        return result;
    }

    @PostMapping("/bases/{kbId}/documents")
    public Map<String, Object> importDocument(
            @PathVariable String kbId,
            @RequestBody Map<String, String> request) {
        
        String title = request.get("title");
        String content = request.get("content");
        String type = request.getOrDefault("type", "TXT");
        
        KnowledgeBaseService.KnowledgeDocument doc = knowledgeBaseService.importDocument(kbId, title, content, type);
        
        Map<String, Object> result = new HashMap<>();
        if (doc != null) {
            result.put("success", true);
            result.put("id", doc.getId());
            result.put("title", doc.getTitle());
            result.put("status", doc.getStatus());
            result.put("chunks", doc.getChunks() != null ? doc.getChunks().size() : 0);
        } else {
            result.put("success", false);
            result.put("message", "Knowledge base not found");
        }
        return result;
    }

    @GetMapping("/bases/{kbId}/documents")
    public Map<String, Object> getDocuments(@PathVariable String kbId) {
        List<KnowledgeBaseService.KnowledgeDocument> docs = knowledgeBaseService.getDocuments(kbId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("documents", docs);
        result.put("total", docs.size());
        return result;
    }

    @DeleteMapping("/documents/{docId}")
    public Map<String, Object> deleteDocument(@PathVariable String docId) {
        boolean success = knowledgeBaseService.deleteDocument(docId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Deleted" : "Not found");
        return result;
    }

    @GetMapping("/bases/{kbId}/search")
    public Map<String, Object> search(
            @PathVariable String kbId,
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        
        List<KnowledgeBaseService.SearchResult> results = knowledgeBaseService.search(kbId, query, topK);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("query", query);
        result.put("results", results);
        result.put("total", results.size());
        return result;
    }

    @PostMapping("/bases/{kbId}/ask")
    public Map<String, Object> ask(
            @PathVariable String kbId,
            @RequestBody Map<String, String> request) {
        
        String question = request.get("question");
        
        List<KnowledgeBaseService.SearchResult> results = knowledgeBaseService.search(kbId, question, 3);
        
        StringBuilder answer = new StringBuilder();
        if (results.isEmpty()) {
            answer.append("Sorry, I could not find relevant information.");
        } else {
            answer.append("Based on the knowledge base, I found the following information:\n\n");
            for (int i = 0; i < results.size(); i++) {
                KnowledgeBaseService.SearchResult r = results.get(i);
                answer.append(i + 1).append(". ").append(r.getContent()).append("\n\n");
            }
            answer.append("--- Above content from knowledge base retrieval");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("question", question);
        result.put("answer", answer.toString());
        result.put("sources", results.size());
        return result;
    }
}
