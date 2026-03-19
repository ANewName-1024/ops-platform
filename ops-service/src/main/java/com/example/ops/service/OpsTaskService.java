package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 运维任务管理服务
 */
@Service
public class OpsTaskService {

    private final Map<String, OpsTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(1);

    /**
     * 任务状态
     */
    public enum TaskStatus {
        PENDING, RUNNING, SUCCESS, FAILED, CANCELLED
    }

    /**
     * 运维任务
     */
    public static class OpsTask {
        private final String taskId;
        private final String taskType;
        private final String target;
        private final String content;
        private TaskStatus status;
        private String result;
        private String executedBy;
        private final long createdAt;
        private long executedAt;
        private long completedAt;

        public OpsTask(String taskType, String target, String content) {
            this.taskId = "task-" + System.currentTimeMillis();
            this.taskType = taskType;
            this.target = target;
            this.content = content;
            this.status = TaskStatus.PENDING;
            this.createdAt = System.currentTimeMillis();
        }

        // Getters
        public String getTaskId() { return taskId; }
        public String getTaskType() { return taskType; }
        public String getTarget() { return target; }
        public String getContent() { return content; }
        public TaskStatus getStatus() { return status; }
        public String getResult() { return result; }
        public String getExecutedBy() { return executedBy; }
        public long getCreatedAt() { return createdAt; }
        public long getExecutedAt() { return executedAt; }
        public long getCompletedAt() { return completedAt; }

        // Setters
        public void setStatus(TaskStatus status) { this.status = status; }
        public void setResult(String result) { this.result = result; }
        public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }
        public void setExecutedAt(long executedAt) { this.executedAt = executedAt; }
        public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    }

    /**
     * 创建任务
     */
    public OpsTask createTask(String taskType, String target, String content) {
        OpsTask task = new OpsTask(taskType, target, content);
        tasks.put(task.getTaskId(), task);
        return task;
    }

    /**
     * 获取任务
     */
    public OpsTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * 获取所有任务
     */
    public List<OpsTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * 更新任务状态
     */
    public void updateTaskStatus(String taskId, TaskStatus status, String result) {
        OpsTask task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(status);
            task.setResult(result);
            if (status == TaskStatus.RUNNING) {
                task.setExecutedAt(System.currentTimeMillis());
            } else if (status == TaskStatus.SUCCESS || status == TaskStatus.FAILED) {
                task.setCompletedAt(System.currentTimeMillis());
            }
        }
    }

    /**
     * 删除任务
     */
    public boolean deleteTask(String taskId) {
        return tasks.remove(taskId) != null;
    }
}
