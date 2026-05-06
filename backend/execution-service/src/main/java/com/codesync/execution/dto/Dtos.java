//package com.codesync.execution.dto;
//
//import lombok.Data;
//
//// ── Submit ──────────────────────────────────────────────────────
//@Data
//public class ExecuteRequest {
//    private String language;  // e.g. "python"
//    private String code;
//    private String stdin;     // optional
//    private String projectId; // optional
//    private String fileId;    // optional
//}
//
//// ── Job submitted response ───────────────────────────────────────
//@Data
//public class ExecuteResponse {
//    private String jobId;
//    private String status;
//    private String message;
//}
//
//// ── Full job result ──────────────────────────────────────────────
//@Data
//public class JobResult {
//    private String jobId;
//    private String status;
//    private String stdout;
//    private String stderr;
//    private Integer exitCode;
//    private Long executionTimeMs;
//    private String language;
//    private String createdAt;
//    private String finishedAt;
//}
//
//// ── RabbitMQ message payload ─────────────────────────────────────
//@Data
//public class ExecutionMessage {
//    private String jobId;
//    private String userId;
//    private String language;
//    private String code;
//    private String stdin;
//    private String sandboxImage;
//    private String runCommand;
//    private String compileCommand;
//}
//
//// ── WebSocket stream chunk ───────────────────────────────────────
//@Data
//public class OutputChunk {
//    private String jobId;
//    private String type;   // "stdout" | "stderr" | "status" | "done"
//    private String data;
//    private Integer exitCode;
//    private Long executionTimeMs;
//}
