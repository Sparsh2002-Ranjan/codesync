package com.codesync.execution.service;

import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.repository.ExecutionJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Separate Spring bean so @Async proxy is applied correctly.
 * Never call this from within the same class — always inject and call via Spring.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncExecutionRunner {

    private final ExecutionJobRepository jobRepository;

    private static final Map<String, String> SIMULATED_OUTPUTS = Map.of(
        "java",       "Hello, CodeSync! ☕\nExecution completed successfully.",
        "python",     "Hello, CodeSync! 🐍\nExecution completed successfully.",
        "javascript", "Hello, CodeSync! 🟨\nExecution completed successfully.",
        "typescript", "Hello, CodeSync! 🔷\nExecution completed successfully.",
        "c",          "Hello, CodeSync! ⚡\nExecution completed successfully.",
        "cpp",        "Hello, CodeSync! ⚡\nExecution completed successfully.",
        "go",         "Hello, CodeSync! 🐹\nExecution completed successfully.",
        "rust",       "Hello, CodeSync! 🦀\nExecution completed successfully."
    );

    @Async
    @Transactional
    public void executeAsync(String jobId) {
        ExecutionJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        try {
            job.setStatus(ExecutionJob.Status.RUNNING);        // FIX: was JobStatus
            job.setStartedAt(Instant.now());                   // FIX: was LocalDateTime.now()
            jobRepository.save(job);

            long startTime = System.currentTimeMillis();
            Thread.sleep(200 + (long) (Math.random() * 300));
            long executionTime = System.currentTimeMillis() - startTime;

            String lang = job.getLanguage().toLowerCase();
            String output = (job.getSourceCode() != null && job.getSourceCode().length() < 5000)
                    ? simulateOutput(job.getSourceCode(), lang)
                    : SIMULATED_OUTPUTS.getOrDefault(lang, "Program executed successfully.");

            job.setStdout(output);
            job.setStderr("");
            job.setExitCode(0);
            job.setExecutionTimeMs(executionTime);
            // FIX: removed setMemoryUsedKb (field does not exist)
            job.setStatus(ExecutionJob.Status.COMPLETED);      // FIX: was JobStatus
            job.setFinishedAt(Instant.now());                  // FIX: was setCompletedAt / LocalDateTime

            log.info("Job {} completed in {}ms", jobId, executionTime);

        } catch (InterruptedException e) {
            job.setStatus(ExecutionJob.Status.TIMEOUT);        // FIX: was JobStatus
            job.setStderr("Execution timed out");
            job.setExitCode(124);
            job.setFinishedAt(Instant.now());                  // FIX: was setCompletedAt
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            job.setStatus(ExecutionJob.Status.FAILED);         // FIX: was JobStatus
            job.setStderr(e.getMessage());
            job.setExitCode(1);
            job.setFinishedAt(Instant.now());                  // FIX: was setCompletedAt
        }

        jobRepository.save(job);
    }

    private String simulateOutput(String code, String lang) {
        StringBuilder output = new StringBuilder();
        for (String line : code.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.contains("System.out.print") || trimmed.contains("print(") ||
                trimmed.contains("console.log") || trimmed.contains("fmt.Print") ||
                trimmed.contains("println!") || trimmed.contains("printf")) {
                int start = trimmed.indexOf('"');
                int end   = trimmed.lastIndexOf('"');
                if (start >= 0 && end > start) {
                    output.append(trimmed, start + 1, end).append("\n");
                }
            }
        }
        return output.isEmpty()
                ? SIMULATED_OUTPUTS.getOrDefault(lang, "Program executed successfully.")
                : output.toString().trim();
    }
}
