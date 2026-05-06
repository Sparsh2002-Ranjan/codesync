package com.codesync.execution.repository;

import com.codesync.execution.entity.ExecutionJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionJobRepository extends JpaRepository<ExecutionJob, String> {
    List<ExecutionJob> findByUserIdOrderByCreatedAtDesc(String userId);
    List<ExecutionJob> findByProjectIdOrderByCreatedAtDesc(String projectId);
}
