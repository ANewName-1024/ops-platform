package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流服务
 * 支持定时任务、触发器、工作流编排
 */
@Service
public class WorkflowService {

    private final Map<String, Workflow> workflows = new ConcurrentHashMap<>();
    private final Map<String, WorkflowExecution> executions = new ConcurrentHashMap<>();

    /**
     * 工作流定义
     */
    public static class Workflow {
        private final String id;
        private final String name;
        private final String description;
        private final List<WorkflowStep> steps;
        private final TriggerConfig trigger;
        private boolean enabled = true;
        private final long createdAt;

        public Workflow(String name, String description, List<WorkflowStep> steps, TriggerConfig trigger) {
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
        public List<WorkflowStep> getSteps() { return steps; }
        public TriggerConfig getTrigger() { return trigger; }
        public boolean isEnabled() { return enabled; }
        public long getCreatedAt() { return createdAt; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * 工作流步骤
     */
    public static class WorkflowStep {
        private final String id;
        private final String type; // SCRIPT, DELAY, CONDITION, NOTIFY
        private final Map<String, Object> config;
        private final String nextStepId;

        public WorkflowStep(String type, Map<String, Object> config) {
            this.id = "step-" + UUID.randomUUID().toString().substring(0, 8);
            this.type = type;
            this.config = config;
            this.nextStepId = null;
        }

        public WorkflowStep(String type, Map<String, Object> config, String nextStepId) {
            this.id = "step-" + UUID.randomUUID().toString().substring(0, 8);
            this.type = type;
            this.config = config;
            this.nextStepId = nextStepId;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public Map<String, Object> getConfig() { return config; }
        public String getNextStepId() { return nextStepId; }
    }

    /**
     * 触发器配置
     */
    public static class TriggerConfig {
        private final String type; // MANUAL, SCHEDULE, WEBHOOK
        private final String cron; // for SCHEDULE type
        private final String webhookPath; // for WEBHOOK type

        public TriggerConfig(String type) {
            this.type = type;
            this.cron = null;
            this.webhookPath = null;
        }

        public TriggerConfig(String type, String cron) {
            this.type = type;
            this.cron = cron;
            this.webhookPath = null;
        }

        public TriggerConfig(String type, String cron, String webhookPath) {
            this.type = type;
            this.cron = cron;
            this.webhookPath = webhookPath;
        }

        public String getType() { return type; }
        public String getCron() { return cron; }
        public String getWebhookPath() { return webhookPath; }
    }

    /**
     * 工作流执行记录
     */
    public static class WorkflowExecution {
        private final String executionId;
        private final String workflowId;
        private final String workflowName;
        private String status; // PENDING, RUNNING, SUCCESS, FAILED
        private final long startTime;
        private long endTime;
        private String result;
        private String error;

        public WorkflowExecution(String workflowId, String workflowName) {
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
    public Workflow createWorkflow(String name, String description, 
                                   List<WorkflowStep> steps, TriggerConfig trigger) {
        Workflow workflow = new Workflow(name, description, steps, trigger);
        workflows.put(workflow.getId(), workflow);
        return workflow;
    }

    /**
     * 获取所有工作流
     */
    public List<Workflow> getAllWorkflows() {
        return new ArrayList<>(workflows.values());
    }

    /**
     * 获取工作流
     */
    public Workflow getWorkflow(String id) {
        return workflows.get(id);
    }

    /**
     * 删除工作流
     */
    public boolean deleteWorkflow(String id) {
        return workflows.remove(id) != null;
    }

    /**
     * 启用/禁用工作流
     */
    public boolean setWorkflowEnabled(String id, boolean enabled) {
        Workflow wf = workflows.get(id);
        if (wf != null) {
            wf.setEnabled(enabled);
            return true;
        }
        return false;
    }

    /**
     * 执行工作流
     */
    public WorkflowExecution executeWorkflow(String workflowId) {
        Workflow workflow = workflows.get(workflowId);
        if (workflow == null || !workflow.isEnabled()) {
            return null;
        }

        WorkflowExecution execution = new WorkflowExecution(workflowId, workflow.getName());
        executions.put(execution.getExecutionId(), execution);

        execution.setStatus("RUNNING");

        try {
            // 执行所有步骤
            StringBuilder results = new StringBuilder();
            for (WorkflowStep step : workflow.getSteps()) {
                String stepResult = executeStep(step);
                results.append(step.getId()).append(": ").append(stepResult).append("\n");
            }
            execution.setStatus("SUCCESS");
            execution.setResult(results.toString());
        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setError(e.getMessage());
        }

        execution.setEndTime(System.currentTimeMillis());
        return execution;
    }

    /**
     * 执行单个步骤
     */
    private String executeStep(WorkflowStep step) {
        switch (step.getType()) {
            case "SCRIPT":
                // TODO: 调用 ScriptExecutionService
                return "Script executed: " + step.getConfig().get("script");
            case "DELAY":
                long delay = Long.parseLong(step.getConfig().getOrDefault("millis", "1000").toString());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Delayed " + delay + "ms";
            case "NOTIFY":
                // TODO: 调用通知服务
                return "Notification sent: " + step.getConfig().get("message");
            case "CONDITION":
                return "Condition evaluated";
            default:
                return "Unknown step type: " + step.getType();
        }
    }

    /**
     * 获取执行历史
     */
    public List<WorkflowExecution> getExecutions(String workflowId) {
        List<WorkflowExecution> result = new ArrayList<>();
        for (WorkflowExecution exec : executions.values()) {
            if (workflowId == null || exec.getWorkflowId().equals(workflowId)) {
                result.add(exec);
            }
        }
        return result;
    }

    /**
     * 获取执行详情
     */
    public WorkflowExecution getExecution(String executionId) {
        return executions.get(executionId);
    }
}
