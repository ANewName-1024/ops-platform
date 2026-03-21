package com.example.workflow.repository;

import com.example.workflow.model.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    List<WorkflowInstance> findByDefinitionId(Long definitionId);
    List<WorkflowInstance> findByStartedBy(Long startedBy);
    List<WorkflowInstance> findByStatus(String status);
}
