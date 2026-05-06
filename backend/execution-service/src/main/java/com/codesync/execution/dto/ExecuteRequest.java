package com.codesync.execution.dto;

import lombok.Data;

@Data
public class ExecuteRequest {
    private String language;
    private String code;
    private String stdin;
    private String projectId;
    private String fileId;
}
