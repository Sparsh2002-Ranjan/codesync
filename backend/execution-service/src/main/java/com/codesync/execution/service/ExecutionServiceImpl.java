package com.codesync.execution.service;

import com.codesync.execution.dto.ExecutionMessage;
import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.entity.SupportedLanguage;
import com.codesync.execution.repository.ExecutionJobRepository;
import com.codesync.execution.repository.SupportedLanguageRepository;
import com.codesync.execution.worker.ExecutionWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionJobRepository jobRepository;
    private final SupportedLanguageRepository languageRepository;
    private final ExecutionWorker executionWorker;   // @Async proxy — must be injected, not newed

    @Override
    @Transactional
    public ExecutionJob submitJob(ExecutionJob job) {
        // Normalise language to lowercase so DB lookup and filename resolution always match
        job.setLanguage(job.getLanguage().toLowerCase());

        SupportedLanguage lang = languageRepository.findById(job.getLanguage())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported language: " + job.getLanguage()));

        job.setStatus(ExecutionJob.Status.QUEUED);
        ExecutionJob saved = jobRepository.save(job);

        // Build the message the worker needs
        ExecutionMessage msg = new ExecutionMessage();
        msg.setJobId(saved.getJobId());
        msg.setUserId(saved.getUserId());
        msg.setLanguage(saved.getLanguage());
        msg.setCode(saved.getSourceCode());
        msg.setStdin(saved.getStdin());
        msg.setSandboxImage(lang.getSandboxImage());
        msg.setRunCommand(lang.getRunCommand());
        msg.setCompileCommand(lang.getCompileCommand());

        // Fire-and-forget: @Async returns immediately, Docker runs in background thread,
        // output is streamed to /topic/execution/{jobId} over WebSocket.
        // No RabbitMQ needed.
        executionWorker.executeAsync(msg);

        log.info("Job {} queued for language={}", saved.getJobId(), saved.getLanguage());
        return saved;
    }

    @Override
    public ExecutionJob getJobById(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Execution job not found: " + jobId));
    }

    @Override
    public List<ExecutionJob> getJobsByUser(String userId) {
        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<ExecutionJob> getJobsByProject(String projectId) {
        return jobRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    @Override
    public List<SupportedLanguage> getSupportedLanguages() {
        return languageRepository.findAllEnabled();
    }

    @Override
    @Transactional
    public SupportedLanguage addLanguage(SupportedLanguage language) {
        if (languageRepository.existsById(language.getId())) {
            throw new RuntimeException("Language already exists: " + language.getName());
        }
        return languageRepository.save(language);
    }
}
