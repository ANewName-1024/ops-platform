package com.example.knowledge.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    // 内存存储
    private final Map<Long, Map<String, Object>> knowledgeBases = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> documents = new ConcurrentHashMap<>();
    private final AtomicLong kbIdCounter = new AtomicLong(1);
    private final AtomicLong docIdCounter = new AtomicLong(1);

    public KnowledgeController() {
        // 初始化示例数据
        Map<String, Object> kb1 = new HashMap<>();
        kb1.put("id", 1L);
        kb1.put("name", "运维文档");
        kb1.put("description", "系统运维相关文档");
        kb1.put("documentCount", 5);
        kb1.put("createTime", "2026-01-15 10:00:00");
        knowledgeBases.put(1L, kb1);

        Map<String, Object> kb2 = new HashMap<>();
        kb2.put("id", 2L);
        kb2.put("name", "故障排除");
        kb2.put("description", "常见问题与解决方案");
        kb2.put("documentCount", 3);
        kb2.put("createTime", "2026-02-20 14:30:00");
        knowledgeBases.put(2L, kb2);

        kbIdCounter.set(3L);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "knowledge-service");
        result.put("port", 8092);
        return result;
    }

    // ==================== 知识库 CRUD ====================

    /**
     * 获取知识库列表
     * GET /knowledge/bases
     */
    @GetMapping("/bases")
    public Map<String, Object> listBases() {
        Map<String, Object> result = new HashMap<>();
        result.put("bases", new ArrayList<>(knowledgeBases.values()));
        result.put("total", knowledgeBases.size());
        return result;
    }

    /**
     * 获取知识库详情
     * GET /knowledge/bases/{id}
     */
    @GetMapping("/bases/{id}")
    public Map<String, Object> getBase(@PathVariable Long id) {
        Map<String, Object> kb = knowledgeBases.get(id);
        if (kb == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "知识库不存在");
            return error;
        }
        return kb;
    }

    /**
     * 创建知识库
     * POST /knowledge/bases
     */
    @PostMapping("/bases")
    public Map<String, Object> createBase(@RequestBody Map<String, Object> params) {
        Long id = kbIdCounter.getAndIncrement();
        Map<String, Object> kb = new HashMap<>();
        kb.put("id", id);
        kb.put("name", params.get("name"));
        kb.put("description", params.get("description"));
        kb.put("documentCount", 0);
        kb.put("createTime", LocalDateTime.now().toString());
        knowledgeBases.put(id, kb);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", id);
        result.put("message", "知识库创建成功");
        return result;
    }

    /**
     * 更新知识库
     * PUT /knowledge/bases/{id}
     */
    @PutMapping("/bases/{id}")
    public Map<String, Object> updateBase(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> kb = knowledgeBases.get(id);
        if (kb == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "知识库不存在");
            return error;
        }

        if (params.containsKey("name")) {
            kb.put("name", params.get("name"));
        }
        if (params.containsKey("description")) {
            kb.put("description", params.get("description"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "知识库更新成功");
        return result;
    }

    /**
     * 删除知识库
     * DELETE /knowledge/bases/{id}
     */
    @DeleteMapping("/bases/{id}")
    public Map<String, Object> deleteBase(@PathVariable Long id) {
        if (!knowledgeBases.containsKey(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "知识库不存在");
            return error;
        }
        knowledgeBases.remove(id);

        // 同时删除该知识库下的所有文档
        documents.entrySet().removeIf(entry -> 
            id.equals(entry.getValue().get("kbId")));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "知识库删除成功");
        return result;
    }

    // ==================== 文档 CRUD ====================

    /**
     * 获取知识库下的文档列表
     * GET /knowledge/bases/{kbId}/documents
     */
    @GetMapping("/bases/{kbId}/documents")
    public Map<String, Object> listDocuments(@PathVariable Long kbId) {
        if (!knowledgeBases.containsKey(kbId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "知识库不存在");
            return error;
        }

        List<Map<String, Object>> docList = new ArrayList<>();
        for (Map<String, Object> doc : documents.values()) {
            if (kbId.equals(doc.get("kbId"))) {
                docList.add(doc);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("documents", docList);
        result.put("total", docList.size());
        return result;
    }

    /**
     * 添加文档
     * POST /knowledge/bases/{kbId}/documents
     */
    @PostMapping("/bases/{kbId}/documents")
    public Map<String, Object> addDocument(@PathVariable Long kbId, @RequestBody Map<String, Object> params) {
        if (!knowledgeBases.containsKey(kbId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "知识库不存在");
            return error;
        }

        Long docId = docIdCounter.getAndIncrement();
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", docId);
        doc.put("kbId", kbId);
        doc.put("title", params.get("title"));
        doc.put("content", params.get("content"));
        doc.put("type", params.getOrDefault("type", "markdown"));
        doc.put("createTime", LocalDateTime.now().toString());
        documents.put(docId, doc);

        // 更新知识库文档计数
        Map<String, Object> kb = knowledgeBases.get(kbId);
        kb.put("documentCount", ((Number) kb.get("documentCount")).intValue() + 1);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", docId);
        result.put("message", "文档添加成功");
        return result;
    }

    /**
     * 删除文档
     * DELETE /knowledge/documents/{id}
     */
    @DeleteMapping("/documents/{id}")
    public Map<String, Object> deleteDocument(@PathVariable Long id) {
        Map<String, Object> doc = documents.get(id);
        if (doc == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文档不存在");
            return error;
        }

        Long kbId = (Long) doc.get("kbId");
        documents.remove(id);

        // 更新知识库文档计数
        if (knowledgeBases.containsKey(kbId)) {
            Map<String, Object> kb = knowledgeBases.get(kbId);
            kb.put("documentCount", Math.max(0, ((Number) kb.get("documentCount")).intValue() - 1));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "文档删除成功");
        return result;
    }

    /**
     * 获取文档详情
     * GET /knowledge/documents/{id}
     */
    @GetMapping("/documents/{id}")
    public Map<String, Object> getDocument(@PathVariable Long id) {
        Map<String, Object> doc = documents.get(id);
        if (doc == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文档不存在");
            return error;
        }
        return doc;
    }

    // ==================== 搜索 ====================

    /**
     * 搜索知识库
     * GET /knowledge/search?q=keyword
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam("q") String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Map<String, Object> kb : knowledgeBases.values()) {
            String name = String.valueOf(kb.getOrDefault("name", "")).toLowerCase();
            String desc = String.valueOf(kb.getOrDefault("description", "")).toLowerCase();
            if (name.contains(lowerQuery) || desc.contains(lowerQuery)) {
                results.add(kb);
            }
        }

        for (Map<String, Object> doc : documents.values()) {
            String title = String.valueOf(doc.getOrDefault("title", "")).toLowerCase();
            String content = String.valueOf(doc.getOrDefault("content", "")).toLowerCase();
            if (title.contains(lowerQuery) || content.contains(lowerQuery)) {
                results.add(doc);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("results", results);
        result.put("total", results.size());
        return result;
    }

    // 兼容旧接口
    @GetMapping("/list")
    public Map<String, Object> list() {
        return listBases();
    }
}
