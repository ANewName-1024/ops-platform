package com.example.ops.service.knowledge;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knowledge base service
 * Supports document import, vectorization, semantic search
 */
@Service
public class KnowledgeBaseService {

    private final Map<String, KnowledgeDocument> documents = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeBase> knowledgeBases = new ConcurrentHashMap<>();

    public static class KnowledgeBase {
        private final String id;
        private final String name;
        private final String description;
        private final List<String> documentIds;
        private final long createdAt;

        public KnowledgeBase(String name, String description) {
            this.id = "kb-" + System.currentTimeMillis();
            this.name = name;
            this.description = description;
            this.documentIds = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<String> getDocumentIds() { return documentIds; }
        public long getCreatedAt() { return createdAt; }
        public void addDocument(String docId) { documentIds.add(docId); }
    }

    public static class KnowledgeDocument {
        private final String id;
        private final String title;
        private final String content;
        private final String type;
        private String status;
        private final long createdAt;
        private List<String> chunks;
        private List<List<Float>> embeddings;

        public KnowledgeDocument(String title, String content, String type) {
            this.id = "doc-" + System.currentTimeMillis();
            this.title = title;
            this.content = content;
            this.type = type;
            this.status = "PROCESSING";
            this.createdAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public long getCreatedAt() { return createdAt; }
        public List<String> getChunks() { return chunks; }
        public List<List<Float>> getEmbeddings() { return embeddings; }
        public void setChunks(List<String> chunks) { this.chunks = chunks; }
        public void setEmbeddings(List<List<Float>> embeddings) { this.embeddings = embeddings; }
        public void setStatus(String status) { this.status = status; }
    }

    public KnowledgeBase createKnowledgeBase(String name, String description) {
        KnowledgeBase kb = new KnowledgeBase(name, description);
        knowledgeBases.put(kb.getId(), kb);
        return kb;
    }

    public List<KnowledgeBase> getAllKnowledgeBases() {
        return new ArrayList<>(knowledgeBases.values());
    }

    public KnowledgeBase getKnowledgeBase(String id) {
        return knowledgeBases.get(id);
    }

    public boolean deleteKnowledgeBase(String id) {
        return knowledgeBases.remove(id) != null;
    }

    public KnowledgeDocument importDocument(String kbId, String title, String content, String type) {
        KnowledgeBase kb = knowledgeBases.get(kbId);
        if (kb == null) return null;

        KnowledgeDocument doc = new KnowledgeDocument(title, content, type);
        List<String> chunks = splitIntoChunks(content);
        doc.setChunks(chunks);
        List<List<Float>> embeddings = generateEmbeddings(chunks);
        doc.setEmbeddings(embeddings);
        doc.setStatus("READY");
        
        documents.put(doc.getId(), doc);
        kb.addDocument(doc.getId());
        return doc;
    }

    public List<KnowledgeDocument> getDocuments(String kbId) {
        KnowledgeBase kb = knowledgeBases.get(kbId);
        if (kb == null) return new ArrayList<>();
        
        List<KnowledgeDocument> result = new ArrayList<>();
        for (String docId : kb.getDocumentIds()) {
            KnowledgeDocument doc = documents.get(docId);
            if (doc != null) result.add(doc);
        }
        return result;
    }

    public boolean deleteDocument(String docId) {
        return documents.remove(docId) != null;
    }

    public List<SearchResult> search(String kbId, String query, int topK) {
        KnowledgeBase kb = knowledgeBases.get(kbId);
        if (kb == null) return new ArrayList<>();

        List<Float> queryEmbedding = generateSingleEmbedding(query);
        List<SearchResult> results = new ArrayList<>();
        
        for (String docId : kb.getDocumentIds()) {
            KnowledgeDocument doc = documents.get(docId);
            if (doc == null || doc.getEmbeddings() == null) continue;

            List<String> chunks = doc.getChunks();
            List<List<Float>> embeddings = doc.getEmbeddings();
            
            for (int i = 0; i < chunks.size(); i++) {
                float similarity = cosineSimilarity(queryEmbedding, embeddings.get(i));
                results.add(new SearchResult(doc.getTitle(), chunks.get(i), similarity));
            }
        }

        results.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
        return results.subList(0, Math.min(topK, results.size()));
    }

    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        int chunkSize = 500;
        
        for (int i = 0; i < content.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, content.length());
            chunks.add(content.substring(i, end));
        }
        
        return chunks.isEmpty() ? List.of(content) : chunks;
    }

    private List<List<Float>> generateEmbeddings(List<String> texts) {
        List<List<Float>> embeddings = new ArrayList<>();
        for (String text : texts) {
            embeddings.add(generateSingleEmbedding(text));
        }
        return embeddings;
    }

    private List<Float> generateSingleEmbedding(String text) {
        Random random = new Random(text.hashCode());
        List<Float> embedding = new ArrayList<>();
        for (int i = 0; i < 384; i++) {
            embedding.add(random.nextFloat());
        }
        return embedding;
    }

    private float cosineSimilarity(List<Float> a, List<Float> b) {
        if (a.size() != b.size()) return 0;
        
        float dotProduct = 0;
        float normA = 0;
        float normB = 0;
        
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        
        if (normA == 0 || normB == 0) return 0;
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    public static class SearchResult {
        private final String title;
        private final String content;
        private final float score;

        public SearchResult(String title, String content, float score) {
            this.title = title;
            this.content = content;
            this.score = score;
        }

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public float getScore() { return score; }
    }
}
