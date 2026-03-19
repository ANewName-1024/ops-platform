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

    @GetMapping("/bases/{kbId}")
    public Map<String, Object> getKnowledgeBase(@PathVariable("id") String id) {
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
    public Map<String, Object> deleteKnowledgeBase(@PathVariable("id") String id) {
        boolean success = knowledgeBaseService.deleteKnowledgeBase(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Deleted" : "Not found");
        return result;
    }

    @PostMapping("/bases/{kbId}/documents")
    public Map<String, Object> importDocument(
            @PathVariable("kbId") String kbId,
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
    public Map<String, Object> getDocuments(@PathVariable("kbId") String kbId) {
        List<KnowledgeBaseService.KnowledgeDocument> docs = knowledgeBaseService.getDocuments(kbId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("documents", docs);
        result.put("total", docs.size());
        return result;
    }

    @DeleteMapping("/documents/{docId}")
    public Map<String, Object> deleteDocument(@PathVariable("docId") String docId) {
        boolean success = knowledgeBaseService.deleteDocument(docId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Deleted" : "Not found");
        return result;
    }

    @GetMapping("/bases/{kbId}/search")
    public Map<String, Object> search(
            @PathVariable("kbId") String kbId,
            @RequestParam("query") String query,
            @RequestParam(name = "topK", defaultValue = "5") int topK) {
        
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
            @PathVariable("kbId") String kbId,
            @RequestBody Map<String, String> request) {
        
        String question = request.get("question");
        
        // Use AI-powered Q&A
        Map<String, Object> aiResult = knowledgeBaseService.askWithAI(kbId, question);
        return aiResult;
    }
}
