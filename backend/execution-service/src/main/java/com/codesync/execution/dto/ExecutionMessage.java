package com.codesync.execution.dto;

import lombok.Data;

@Data
public class ExecutionMessage {
    private String jobId;
    private String userId;
    private String language;
    private String code;
    private String stdin;
    private String sandboxImage;
    private String runCommand;
    private String compileCommand;
}
