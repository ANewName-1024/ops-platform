package com.example.workflow.controller;

import com.example.workflow.service.WorkflowService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;
    
    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "workflow-service");
        result.put("port", 8091);
        return result;
    }

    @GetMapping
    public Map<String, Object> list() {
        return workflowService.getWorkflowList();
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> params) {
        return workflowService.createWorkflow(params);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return workflowService.getWorkflowDetail(id);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return workflowService.updateWorkflow(id, params);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        return workflowService.deleteWorkflow(id);
    }

    @PostMapping("/{id}/execute")
    public Map<String, Object> execute(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        return workflowService.executeWorkflow(id, params);
    }

    @GetMapping("/{id}/executions")
    public Map<String, Object> executions(@PathVariable Long id) {
        return workflowService.getExecutionHistory(id);
    }
}
