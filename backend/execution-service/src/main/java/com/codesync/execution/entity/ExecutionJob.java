package com.codesync.execution.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionJob {

    public enum Status { QUEUED, RUNNING, COMPLETED, FAILED, TIMEOUT, CANCELLED }

    @Id
    @Column(name = "job_id")
    @Builder.Default
    private String jobId = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userId;

    private String projectId;
    private String fileId;

    @Column(nullable = false)
    private String language;

    /** Source code — also aliased as 'code' in DB column */
    @Column(name = "code", columnDefinition = "TEXT", nullable = false)
    private String sourceCode;

    /** Optional stdin provided by the user */
    @Column(columnDefinition = "TEXT")
    private String stdin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.QUEUED;

    @Column(columnDefinition = "TEXT")
    private String stdout;

    @Column(columnDefinition = "TEXT")
    private String stderr;

    private Integer exitCode;

    /** Wall-clock ms the container ran */
    private Long executionTimeMs;

    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant startedAt;
    private Instant finishedAt;

    /** Docker container ID while running */
    private String containerId;

    // ── Convenience accessor so worker code using .getCode() still compiles ──
    public String getCode() { return sourceCode; }
    public void setCode(String code) { this.sourceCode = code; }
}
