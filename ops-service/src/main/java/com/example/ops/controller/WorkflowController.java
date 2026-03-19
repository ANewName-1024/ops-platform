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
        String name = (String) request.get("name");
        String description = (String) request.getOrDefault("description", "");
        
        // 解析步骤
        List<WorkflowService.WorkflowStep> steps = new ArrayList<>();
        if (request.containsKey("steps")) {
            List<Map<String, Object>> stepConfigs = (List<Map<String, Object>>) request.get("steps");
            for (Map<String, Object> stepConfig : stepConfigs) {
                String type = (String) stepConfig.get("type");
                Map<String, Object> config = (Map<String, Object>) stepConfig.getOrDefault("config", new HashMap<>());
                WorkflowService.WorkflowStep step = new WorkflowService.WorkflowStep(type, config);
                steps.add(step);
            }
        }

        // 解析触发器
        WorkflowService.TriggerConfig trigger = null;
        if (request.containsKey("trigger")) {
            Map<String, Object> triggerConfig = (Map<String, Object>) request.get("trigger");
            String triggerType = (String) triggerConfig.get("type");
            String cron = (String) triggerConfig.getOrDefault("cron", null);
            String webhookPath = (String) triggerConfig.getOrDefault("webhookPath", null);
            
            if (cron != null) {
                trigger = new WorkflowService.TriggerConfig(triggerType, cron);
            } else if (webhookPath != null) {
                trigger = new WorkflowService.TriggerConfig(triggerType, null, webhookPath);
            } else {
                trigger = new WorkflowService.TriggerConfig(triggerType);
            }
        } else {
            trigger = new WorkflowService.TriggerConfig("MANUAL");
        }

        WorkflowService.Workflow workflow = workflowService.createWorkflow(name, description, steps, trigger);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("workflowId", workflow.getId());
        result.put("name", workflow.getName());
        result.put("status", "created");
        return result;
    }

    /**
     * 获取所有工作流
     */
    @GetMapping
    public Map<String, Object> getWorkflows() {
        List<WorkflowService.Workflow> workflows = workflowService.getAllWorkflows();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("workflows", workflows);
        result.put("total", workflows.size());
        return result;
    }

    /**
     * 获取工作流详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getWorkflow(@PathVariable String id) {
        WorkflowService.Workflow workflow = workflowService.getWorkflow(id);
        Map<String, Object> result = new HashMap<>();
        if (workflow != null) {
            result.put("success", true);
            result.put("workflow", workflow);
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
        result.put("message", success ? "Workflow deleted" : "Failed to delete");
        return result;
    }

    /**
     * 启用/禁用工作流
     */
    @PutMapping("/{id}/enable")
    public Map<String, Object> setWorkflowEnabled(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {
        
        boolean enabled = request.getOrDefault("enabled", true);
        boolean success = workflowService.setWorkflowEnabled(id, enabled);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("enabled", enabled);
        return result;
    }

    /**
     * 执行工作流
     */
    @PostMapping("/{id}/execute")
    public Map<String, Object> executeWorkflow(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            WorkflowService.WorkflowExecution execution = workflowService.executeWorkflow(id);
            
            if (execution != null) {
                result.put("success", true);
                result.put("executionId", execution.getExecutionId());
                result.put("status", execution.getStatus());
                result.put("result", execution.getResult());
                if (execution.getError() != null) {
                    result.put("error", execution.getError());
                }
            } else {
                result.put("success", false);
                result.put("message", "Workflow not found or disabled");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Execution error: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取执行历史
     */
    @GetMapping("/{id}/executions")
    public Map<String, Object> getExecutions(@PathVariable String id) {
        List<WorkflowService.WorkflowExecution> executions = workflowService.getExecutions(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("executions", executions);
        result.put("total", executions.size());
        return result;
    }

    /**
     * 获取执行详情
     */
    @GetMapping("/executions/{executionId}")
    public Map<String, Object> getExecution(@PathVariable String executionId) {
        WorkflowService.WorkflowExecution execution = workflowService.getExecution(executionId);
        Map<String, Object> result = new HashMap<>();
        if (execution != null) {
            result.put("success", true);
            result.put("execution", execution);
        } else {
            result.put("success", false);
            result.put("message", "Execution not found");
        }
        return result;
    }
}
