package com.example.ops.controller;

import com.example.ops.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 工作流 REST API
 */
@RestController
@RequestMapping("/ops/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    /**
     * 创建工作流
     */
    @PostMapping
    public Map<String, Object> createWorkflow(@RequestBody Map<String, Object> request) {
        String name = (String) request.getOrDefault("name", "Untitled");
        String description = (String) request.getOrDefault("description", "");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> steps = (List<Map<String, Object>>) request.get("steps");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> trigger = (Map<String, Object>) request.get("trigger");
        
        WorkflowService.WorkflowDefinition wf = workflowService.createWorkflow(name, description, steps, trigger);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("workflowId", wf.getId());
        result.put("name", wf.getName());
        result.put("status", "created");
        return result;
    }

    /**
     * 获取所有工作流
     */
    @GetMapping
    public Map<String, Object> getWorkflows() {
        List<WorkflowService.WorkflowDefinition> wfs = workflowService.getAllWorkflows();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("workflows", wfs);
        result.put("total", wfs.size());
        return result;
    }

    /**
     * 获取工作流详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getWorkflow(@PathVariable String id) {
        WorkflowService.WorkflowDefinition wf = workflowService.getWorkflow(id);
        Map<String, Object> result = new HashMap<>();
        if (wf != null) {
            result.put("success", true);
            result.put("workflow", wf);
        } else {
            result.put("success", false);
            result.put("message", "Workflow not found");
        }
        return result;
    }

    /**
     * 删除工作流
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteWorkflow(@PathVariable String id) {
        boolean success = workflowService.deleteWorkflow(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Deleted" : "Not found");
        return result;
    }

    /**
     * 执行工作流
     */
    @PostMapping("/{id}/execute")
    public Map<String, Object> executeWorkflow(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            WorkflowService.ExecutionRecord rec = workflowService.executeWorkflow(id);
            if (rec != null) {
                result.put("success", true);
                result.put("executionId", rec.getExecutionId());
                result.put("status", rec.getStatus());
                result.put("result", rec.getResult());
            } else {
                result.put("success", false);
                result.put("message", "Not found or disabled");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取执行历史
     */
    @GetMapping("/{id}/executions")
    public Map<String, Object> getExecutions(@PathVariable String id) {
        List<WorkflowService.ExecutionRecord> recs = workflowService.getExecutions(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("executions", recs);
        result.put("total", recs.size());
        return result;
    }

    /**
     * 获取执行详情
     */
    @GetMapping("/executions/{executionId}")
    public Map<String, Object> getExecution(@PathVariable String executionId) {
        WorkflowService.ExecutionRecord rec = workflowService.getExecution(executionId);
        Map<String, Object> result = new HashMap<>();
        if (rec != null) {
            result.put("success", true);
            result.put("execution", rec);
        } else {
            result.put("success", false);
            result.put("message", "Not found");
        }
        return result;
    }
}
