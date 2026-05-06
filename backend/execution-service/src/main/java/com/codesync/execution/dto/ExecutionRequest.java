package com.codesync.execution.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExecutionRequest {

    @NotBlank(message = "projectId is required")
    private String projectId;

    private String fileId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "language is required")
    private String language;

    @NotBlank(message = "sourceCode is required")
    private String sourceCode;

    private String stdin;
}
