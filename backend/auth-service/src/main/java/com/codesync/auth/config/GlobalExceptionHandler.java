package com.codesync.auth.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validation errors (@Valid failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status",    400,
                "error",     "Validation Failed",
                "fields",    fieldErrors,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Business logic errors (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("not found")) status = HttpStatus.NOT_FOUND;
            if (ex.getMessage().contains("Invalid email or password")) status = HttpStatus.UNAUTHORIZED;
            if (ex.getMessage().contains("suspended")) status = HttpStatus.FORBIDDEN;
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

    // Generic fallback
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
