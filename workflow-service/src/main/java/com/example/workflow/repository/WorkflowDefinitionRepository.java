package com.example.workflow.repository;

import com.example.workflow.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    List<WorkflowDefinition> findByStatus(String status);
    List<WorkflowDefinition> findByCreatedBy(Long createdBy);
}
