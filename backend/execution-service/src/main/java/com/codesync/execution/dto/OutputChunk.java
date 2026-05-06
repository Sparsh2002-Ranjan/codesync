package com.codesync.execution.dto;

import lombok.Data;

@Data
public class OutputChunk {
    private String jobId;
    private String type;   // "stdout" | "stderr" | "status" | "done"
    private String data;
    private Integer exitCode;
    private Long executionTimeMs;
}
