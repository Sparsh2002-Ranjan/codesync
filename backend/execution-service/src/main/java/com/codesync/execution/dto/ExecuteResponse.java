package com.codesync.execution.dto;

import lombok.Data;

@Data
public class ExecuteResponse {
    private String jobId;
    private String status;
    private String message;
}
