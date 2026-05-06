package com.codesync.execution.service;

import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.entity.SupportedLanguage;

import java.util.List;

/**
 * Contract for code-execution operations.
 * Implemented by ExecutionServiceImpl.
 */
public interface ExecutionService {

    ExecutionJob submitJob(ExecutionJob job);

    ExecutionJob getJobById(String jobId);

    List<ExecutionJob> getJobsByUser(String userId);

    List<ExecutionJob> getJobsByProject(String projectId);

    List<SupportedLanguage> getSupportedLanguages();

    SupportedLanguage addLanguage(SupportedLanguage language);
}
