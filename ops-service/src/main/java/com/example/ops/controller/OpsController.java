package com.example.ops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import com.example.ops.service.MetricsService;
import com.example.ops.service.TaskSchedulerService;
import com.example.ops.service.ScriptExecutionService;
import com.example.ops.service.OpsTaskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * 运维服务健康检查和指标接口
 */
@RestController
@RequestMapping("/ops")
@RefreshScope
public class OpsController {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    @Autowired
    private OpsTaskService opsTaskService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "ops-service");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取服务指标
     */
    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return metricsService.getMetrics();
    }

    /**
     * 获取日志级别
     */
    @GetMapping("/loggers")
    public Map<String, Object> loggers() {
        Map<String, Object> result = new HashMap<>();
        result.put("loggers", metricsService.getLoggerLevels());
        return result;
    }

    /**
     * 修改日志级别
     */
    @PostMapping("/loggers/{name}/{level}")
    public Map<String, String> setLogLevel(
            @PathVariable String name,
            @PathVariable String level) {
        Map<String, String> result = new HashMap<>();
        result.put("logger", name);
        result.put("level", level);
        result.put("status", "ok");
        return result;
    }

    /**
     * 获取环境信息
     */
    @GetMapping("/env")
    public Map<String, Object> env() {
        Map<String, Object> result = new HashMap<>();
        result.put("java.version", System.getProperty("java.version"));
        result.put("os.name", System.getProperty("os.name"));
        result.put("os.arch", System.getProperty("os.arch"));
        result.put("os.version", System.getProperty("os.version"));
        result.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        result.put("freeMemory", Runtime.getRuntime().freeMemory());
        result.put("maxMemory", Runtime.getRuntime().maxMemory());
        result.put("totalMemory", Runtime.getRuntime().totalMemory());
        return result;
    }

    // ==================== 任务调度 API ====================

    /**
     * 创建运维任务
     */
    @PostMapping("/tasks")
    public Map<String, Object> createTask(@RequestBody Map<String, String> request) {
        String taskType = request.get("taskType");
        String target = request.get("target");
        String content = request.get("content");

        OpsTaskService.OpsTask task = opsTaskService.createTask(taskType, target, content);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("taskId", task.getTaskId());
        result.put("status", task.getStatus());
        return result;
    }

    /**
     * 获取所有任务
     */
    @GetMapping("/tasks")
    public Map<String, Object> getTasks() {
        List<OpsTaskService.OpsTask> allTasks = opsTaskService.getAllTasks();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tasks", allTasks);
        return result;
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/tasks/{taskId}")
    public Map<String, Object> getTask(@PathVariable String taskId) {
        OpsTaskService.OpsTask task = opsTaskService.getTask(taskId);
        Map<String, Object> result = new HashMap<>();
        if (task != null) {
            result.put("success", true);
            result.put("task", task);
        } else {
            result.put("success", false);
            result.put("message", "Task not found");
        }
        return result;
    }

    // ==================== 脚本执行 API ====================

    /**
     * 执行脚本
     */
    @PostMapping("/scripts/execute")
    public Map<String, Object> executeScript(@RequestBody Map<String, String> request) {
        String script = request.get("script");
        String typeStr = request.getOrDefault("type", "SHELL");

        ScriptExecutionService.ScriptType type = ScriptExecutionService.ScriptType.valueOf(typeStr);
        ScriptExecutionService.ExecutionResult result = scriptExecutionService.execute(script, type);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("exitCode", result.getExitCode());
        response.put("output", result.getOutput());
        response.put("error", result.getError());
        response.put("duration", result.getDuration());
        return response;
    }
}
