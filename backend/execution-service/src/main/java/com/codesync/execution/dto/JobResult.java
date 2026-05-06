package com.codesync.execution.dto;

import lombok.Data;

@Data
public class JobResult {
    private String jobId;
    private String status;
    private String stdout;
    private String stderr;
    private Integer exitCode;
    private Long executionTimeMs;
    private String language;
    private String createdAt;
    private String finishedAt;
}
