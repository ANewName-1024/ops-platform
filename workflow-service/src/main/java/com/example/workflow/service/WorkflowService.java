package com.example.workflow.service;

import com.example.workflow.model.WorkflowDefinition;
import com.example.workflow.model.WorkflowInstance;
import com.example.workflow.repository.WorkflowDefinitionRepository;
import com.example.workflow.repository.WorkflowInstanceRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WorkflowService {
    
    private final WorkflowDefinitionRepository definitionRepo;
    private final WorkflowInstanceRepository instanceRepo;
    private final ObjectMapper objectMapper;
    
    public WorkflowService(WorkflowDefinitionRepository definitionRepo, 
                         WorkflowInstanceRepository instanceRepo) {
        this.definitionRepo = definitionRepo;
        this.instanceRepo = instanceRepo;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 获取工作流列表
     */
    public Map<String, Object> getWorkflowList() {
        List<WorkflowDefinition> workflows = definitionRepo.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (WorkflowDefinition w : workflows) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", w.getId());
            item.put("name", w.getName() != null ? w.getName() : "");
            item.put("description", w.getDescription() != null ? w.getDescription() : "");
            item.put("version", w.getVersion());
            item.put("status", w.getStatus() != null ? w.getStatus() : "DRAFT");
            item.put("createdAt", w.getCreatedAt() != null ? w.getCreatedAt().toString() : "");
            list.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("workflows", list);
        result.put("total", list.size());
        return result;
    }
    
    /**
     * 创建工作流
     */
    public Map<String, Object> createWorkflow(Map<String, Object> params) {
        try {
            WorkflowDefinition wf = new WorkflowDefinition();
            wf.setName((String) params.get("name"));
            wf.setDescription((String) params.get("description"));
            wf.setDefinition(objectMapper.writeValueAsString(params.getOrDefault("definition", Map.of())));
            wf.setStatus("DRAFT");
            wf.setVersion(1);
            wf.setCreatedAt(LocalDateTime.now());
            wf.setUpdatedAt(LocalDateTime.now());
            
            wf = definitionRepo.save(wf);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", wf.getId());
            result.put("message", "工作流创建成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取工作流详情
     */
    public Map<String, Object> getWorkflowDetail(Long id) {
        return definitionRepo.findById(id)
            .map(wf -> {
                Map<String, Object> result = new HashMap<>();
                result.put("id", wf.getId());
                result.put("name", wf.getName() != null ? wf.getName() : "");
                result.put("description", wf.getDescription() != null ? wf.getDescription() : "");
                result.put("definition", wf.getDefinition() != null ? wf.getDefinition() : "{}");
                result.put("version", wf.getVersion());
                result.put("status", wf.getStatus() != null ? wf.getStatus() : "DRAFT");
                result.put("createdAt", wf.getCreatedAt() != null ? wf.getCreatedAt().toString() : "");
                return result;
            })
            .orElseGet(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("error", "工作流不存在");
                return result;
            });
    }
    
    /**
     * 更新工作流
     */
    public Map<String, Object> updateWorkflow(Long id, Map<String, Object> params) {
        return definitionRepo.findById(id)
            .map(wf -> {
                if (params.containsKey("name")) wf.setName((String) params.get("name"));
                if (params.containsKey("description")) wf.setDescription((String) params.get("description"));
                if (params.containsKey("definition")) {
                    try {
                        wf.setDefinition(objectMapper.writeValueAsString(params.get("definition")));
                    } catch (Exception e) {}
                }
                if (params.containsKey("status")) wf.setStatus((String) params.get("status"));
                wf.setUpdatedAt(LocalDateTime.now());
                definitionRepo.save(wf);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "更新成功");
                return result;
            })
            .orElseGet(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "工作流不存在");
                return result;
            });
    }
    
    /**
     * 删除工作流
     */
    public Map<String, Object> deleteWorkflow(Long id) {
        if (definitionRepo.existsById(id)) {
            definitionRepo.deleteById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");
            return result;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "工作流不存在");
        return result;
    }
    
    /**
     * 执行工作流
     */
    public Map<String, Object> executeWorkflow(Long id, Map<String, Object> inputData) {
        return definitionRepo.findById(id)
            .map(wf -> {
                if (!"PUBLISHED".equals(wf.getStatus())) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "工作流未发布，无法执行");
                    return result;
                }
                
                // 创建工作流实例
                WorkflowInstance instance = new WorkflowInstance();
                instance.setDefinitionId(id);
                instance.setStatus("RUNNING");
                try {
                    instance.setInputData(inputData != null ? objectMapper.writeValueAsString(inputData) : "{}");
                } catch (Exception e) {
                    instance.setInputData("{}");
                }
                instance.setStartedAt(LocalDateTime.now());
                
                // 解析定义，获取起始节点
                try {
                    JsonNode def = objectMapper.readTree(wf.getDefinition());
                    JsonNode nodes = def.get("nodes");
                    if (nodes != null && nodes.isArray() && nodes.size() > 0) {
                        String startNode = nodes.get(0).get("id").asText();
                        instance.setCurrentNode(startNode);
                    }
                } catch (Exception e) {
                    instance.setCurrentNode("start");
                }
                
                instance = instanceRepo.save(instance);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("instanceId", instance.getId());
                result.put("status", instance.getStatus());
                result.put("currentNode", instance.getCurrentNode() != null ? instance.getCurrentNode() : "");
                result.put("message", "工作流执行成功");
                return result;
            })
            .orElseGet(() -> {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "工作流不存在");
                return result;
            });
    }
    
    /**
     * 获取执行历史
     */
    public Map<String, Object> getExecutionHistory(Long workflowId) {
        List<WorkflowInstance> instances = instanceRepo.findByDefinitionId(workflowId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (WorkflowInstance inst : instances) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", inst.getId());
            item.put("definitionId", inst.getDefinitionId());
            item.put("status", inst.getStatus() != null ? inst.getStatus() : "PENDING");
            item.put("currentNode", inst.getCurrentNode() != null ? inst.getCurrentNode() : "");
            item.put("startedAt", inst.getStartedAt() != null ? inst.getStartedAt().toString() : "");
            item.put("completedAt", inst.getCompletedAt() != null ? inst.getCompletedAt().toString() : "");
            list.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("executions", list);
        result.put("total", list.size());
        return result;
    }
}
