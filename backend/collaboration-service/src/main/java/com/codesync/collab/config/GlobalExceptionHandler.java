package com.codesync.collab.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("not found")) status = HttpStatus.NOT_FOUND;
            if (ex.getMessage().contains("already")) status = HttpStatus.CONFLICT;
            if (ex.getMessage().contains("full") || ex.getMessage().contains("ended")) status = HttpStatus.GONE;
        }
        return ResponseEntity.status(status).body(Map.of(
                "status",    status.value(),
                "error",     status.getReasonPhrase(),
                "message",   ex.getMessage() != null ? ex.getMessage() : "An error occurred",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status",    500,
                "error",     "Internal Server Error",
                "message",   ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
