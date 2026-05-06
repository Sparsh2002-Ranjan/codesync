package com.codesync.execution.controller;

import com.codesync.execution.dto.*;
import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.entity.SupportedLanguage;
import com.codesync.execution.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/execution")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService svc;

    /** POST /api/v1/execution/run — submit a new code execution job */
    @PostMapping("/run")
    public ResponseEntity<ExecuteResponse> run(@RequestBody ExecuteRequest req) {
        // FIX 2: null-safe userId resolution — if authentication is somehow null
        // (should not happen because .anyRequest().authenticated() guards this endpoint)
        // we throw a clear 401-style error instead of a NullPointerException.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String userId = auth.getName();

        ExecutionJob job = ExecutionJob.builder()
                .userId(userId)
                .projectId(req.getProjectId())
                .fileId(req.getFileId())
                .language(req.getLanguage())   // normalised to lowercase inside submitJob()
                .sourceCode(req.getCode())
                .stdin(req.getStdin())
                .build();

        ExecutionJob saved = svc.submitJob(job);

        ExecuteResponse resp = new ExecuteResponse();
        resp.setJobId(saved.getJobId());
        resp.setStatus(saved.getStatus().name());
        resp.setMessage("Job queued. Subscribe to /topic/execution/" + saved.getJobId() + " for live output.");
        return ResponseEntity.accepted().body(resp);
    }

    /** GET /api/v1/execution/jobs/{jobId} — poll job status/result */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobResult> getJob(@PathVariable String jobId) {
        ExecutionJob job = svc.getJobById(jobId);
        return ResponseEntity.ok(toResult(job));
    }

    /** GET /api/v1/execution/jobs/me — current user's job history */
    @GetMapping("/jobs/me")
    public List<JobResult> myJobs() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return svc.getJobsByUser(userId).stream().map(this::toResult).collect(Collectors.toList());
    }

    /** GET /api/v1/execution/jobs/project/{projectId} — project job history */
    @GetMapping("/jobs/project/{projectId}")
    public List<JobResult> projectJobs(@PathVariable String projectId) {
        return svc.getJobsByProject(projectId).stream().map(this::toResult).collect(Collectors.toList());
    }

    /** GET /api/v1/execution/languages — supported language registry */
    @GetMapping("/languages")
    public List<SupportedLanguage> languages() {
        return svc.getSupportedLanguages();
    }

    private JobResult toResult(ExecutionJob j) {
        JobResult r = new JobResult();
        r.setJobId(j.getJobId());
        r.setStatus(j.getStatus().name());
        r.setStdout(j.getStdout());
        r.setStderr(j.getStderr());
        r.setExitCode(j.getExitCode());
        r.setExecutionTimeMs(j.getExecutionTimeMs());
        r.setLanguage(j.getLanguage());
        r.setCreatedAt(j.getCreatedAt() != null ? j.getCreatedAt().toString() : null);
        r.setFinishedAt(j.getFinishedAt() != null ? j.getFinishedAt().toString() : null);
        return r;
    }
}
