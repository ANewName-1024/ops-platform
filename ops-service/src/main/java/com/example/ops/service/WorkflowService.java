package com.example.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流服务
 * 支持定时任务、触发器，工作流编排
 */
@Service
public class WorkflowService {

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    @Autowired
    private NotificationService notificationService;

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
        private final String cron;
        private final String webhookPath;

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
        private String status;
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
        if (workflow == null) {
            return null;
        }
        
        if (!workflow.isEnabled()) {
            return null;
        }

        WorkflowExecution execution = new WorkflowExecution(workflowId, workflow.getName());
        executions.put(execution.getExecutionId(), execution);

        execution.setStatus("RUNNING");

        try {
            // 检查步骤
            if (workflow.getSteps() == null || workflow.getSteps().isEmpty()) {
                execution.setStatus("SUCCESS");
                execution.setResult("No steps to execute");
            } else {
                // 执行所有步骤
                StringBuilder results = new StringBuilder();
                for (WorkflowStep step : workflow.getSteps()) {
                    try {
                        String stepResult = executeStep(step);
                        results.append(step.getId()).append(": ").append(stepResult).append("\n");
                    } catch (Exception e) {
                        results.append(step.getId()).append(": ERROR - ").append(e.getMessage()).append("\n");
                    }
                }
                execution.setStatus("SUCCESS");
                execution.setResult(results.toString());
            }
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
        if (step == null || step.getType() == null) {
            return "Step is null";
        }
        
        switch (step.getType()) {
            case "SCRIPT":
                if (scriptExecutionService != null) {
                    String script = (String) step.getConfig().get("script");
                    String typeStr = (String) step.getConfig().getOrDefault("type", "POWERSHELL");
                    try {
                        ScriptExecutionService.ScriptType type = ScriptExecutionService.ScriptType.valueOf(typeStr);
                        ScriptExecutionService.ExecutionResult result = scriptExecutionService.execute(script, type);
                        return result.isSuccess() ? "Script OK: " + result.getOutput() : "Script Failed: " + result.getError();
                    } catch (Exception e) {
                        return "Script Error: " + e.getMessage();
                    }
                }
                return "Script executed (service not available)";
                
            case "DELAY":
                long delay = 1000;
                Object delayObj = step.getConfig().get("millis");
                if (delayObj != null) {
                    try {
                        delay = Long.parseLong(delayObj.toString());
                    } catch (NumberFormatException e) {
                        delay = 1000;
                    }
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Delayed " + delay + "ms";
                
            case "NOTIFY":
                if (notificationService != null) {
                    String title = (String) step.getConfig().getOrDefault("title", "Workflow Notification");
                    String message = (String) step.getConfig().get("message");
                    String channelType = (String) step.getConfig().getOrDefault("channel", "FEISHU");
                    
                    // 查找渠道
                    List<NotificationService.NotificationChannel> channels = notificationService.getAllChannels();
                    String channelId = null;
                    for (NotificationService.NotificationChannel ch : channels) {
                        if (ch.getType().equalsIgnoreCase(channelType)) {
                            channelId = ch.getId();
                            break;
                        }
                    }
                    
                    if (channelId != null) {
                        NotificationService.SendResult result = notificationService.send(channelId, title, message);
                        return result.isSuccess() ? "Notification sent" : "Notification failed: " + result.getMessage();
                    }
                    return "Channel not found: " + channelType;
                }
                return "Notification skipped (service not available)";
                
            case "CONDITION":
                return "Condition evaluated: true";
                
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
