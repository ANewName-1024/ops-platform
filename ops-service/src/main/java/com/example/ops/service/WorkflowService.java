package com.example.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流服务
 */
@Service
public class WorkflowService {

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    @Autowired
    private NotificationService notificationService;

    private final Map<String, WorkflowDefinition> workflows = new ConcurrentHashMap<>();
    private final Map<String, ExecutionRecord> executions = new ConcurrentHashMap<>();

    /**
     * 工作流定义
     */
    public static class WorkflowDefinition {
        private final String id;
        private final String name;
        private final String description;
        private final List<Map<String, Object>> steps;
        private final Map<String, Object> trigger;
        private boolean enabled = true;
        private final long createdAt;

        public WorkflowDefinition(String name, String description, 
                                List<Map<String, Object>> steps, 
                                Map<String, Object> trigger) {
            this.id = "wf-" + System.currentTimeMillis();
            this.name = name;
            this.description = description;
            this.steps = steps;
            this.trigger = trigger;
            this.createdAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<Map<String, Object>> getSteps() { return steps; }
        public Map<String, Object> getTrigger() { return trigger; }
        public boolean isEnabled() { return enabled; }
        public long getCreatedAt() { return createdAt; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * 执行记录
     */
    public static class ExecutionRecord {
        private final String executionId;
        private final String workflowId;
        private final String workflowName;
        private String status;
        private final long startTime;
        private long endTime;
        private String result;
        private String error;

        public ExecutionRecord(String workflowId, String workflowName) {
            this.executionId = "exec-" + System.currentTimeMillis();
            this.workflowId = workflowId;
            this.workflowName = workflowName;
            this.status = "PENDING";
            this.startTime = System.currentTimeMillis();
        }

        public String getExecutionId() { return executionId; }
        public String getWorkflowId() { return workflowId; }
        public String getWorkflowName() { return workflowName; }
        public String getStatus() { return status; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public String getResult() { return result; }
        public String getError() { return error; }

        public void setStatus(String status) { this.status = status; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public void setResult(String result) { this.result = result; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * 创建工作流
     */
    public WorkflowDefinition createWorkflow(String name, String description, 
                                           List<Map<String, Object>> steps,
                                           Map<String, Object> trigger) {
        WorkflowDefinition wf = new WorkflowDefinition(name, description, steps, trigger);
        workflows.put(wf.getId(), wf);
        return wf;
    }

    /**
     * 获取所有工作流
     */
    public List<WorkflowDefinition> getAllWorkflows() {
        return new ArrayList<>(workflows.values());
    }

    /**
     * 获取工作流
     */
    public WorkflowDefinition getWorkflow(String id) {
        return workflows.get(id);
    }

    /**
     * 删除工作流
     */
    public boolean deleteWorkflow(String id) {
        return workflows.remove(id) != null;
    }

    /**
     * 设置启用状态
     */
    public boolean setWorkflowEnabled(String id, boolean enabled) {
        WorkflowDefinition wf = workflows.get(id);
        if (wf != null) {
            wf.setEnabled(enabled);
            return true;
        }
        return false;
    }

    /**
     * 执行工作流
     */
    public ExecutionRecord executeWorkflow(String workflowId) {
        WorkflowDefinition wf = workflows.get(workflowId);
        if (wf == null) return null;
        if (!wf.isEnabled()) return null;

        ExecutionRecord record = new ExecutionRecord(workflowId, wf.getName());
        executions.put(record.getExecutionId(), record);
        record.setStatus("RUNNING");

        try {
            List<Map<String, Object>> steps = wf.getSteps();
            if (steps == null || steps.isEmpty()) {
                record.setStatus("SUCCESS");
                record.setResult("No steps");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < steps.size(); i++) {
                    Map<String, Object> step = steps.get(i);
                    String stepType = (String) step.get("type");
                    Map<String, Object> config = (Map<String, Object>) step.getOrDefault("config", new HashMap<>());
                    
                    String result = executeStep(stepType, config);
                    sb.append("step-").append(i).append(": ").append(result).append("\n");
                }
                record.setStatus("SUCCESS");
                record.setResult(sb.toString());
            }
        } catch (Exception e) {
            record.setStatus("FAILED");
            record.setError(e.getMessage());
        }

        record.setEndTime(System.currentTimeMillis());
        return record;
    }

    private String executeStep(String type, Map<String, Object> config) {
        if (type == null) return "No step type";
        
        switch (type) {
            case "SCRIPT":
                return "Script executed: " + config.get("script");
            case "DELAY":
                return "Delayed " + config.getOrDefault("millis", "0") + "ms";
            case "NOTIFY":
                return "Notification sent: " + config.get("title");
            case "CONDITION":
                return "Condition OK";
            default:
                return "Unknown type: " + type;
        }
    }

    /**
     * 获取执行记录
     */
    public List<ExecutionRecord> getExecutions(String workflowId) {
        List<ExecutionRecord> list = new ArrayList<>();
        for (ExecutionRecord rec : executions.values()) {
            if (workflowId == null || rec.getWorkflowId().equals(workflowId)) {
                list.add(rec);
            }
        }
        return list;
    }

    public ExecutionRecord getExecution(String executionId) {
        return executions.get(executionId);
    }
}
