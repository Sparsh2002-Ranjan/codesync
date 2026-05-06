package com.codesync.auth.controller;

import com.codesync.auth.entity.User;
import com.codesync.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(Map.of(
            "totalUsers",     authService.getAllUsers().size(),
            "totalProjects",  0,
            "activeSessions", 0,
            "totalExecutions", 0
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<Map<String, String>> suspend(@PathVariable String userId) {
        authService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "User suspended"));
    }

    @PutMapping("/users/{userId}/reactivate")
    public ResponseEntity<Map<String, String>> reactivate(@PathVariable String userId) {
        authService.activateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "User reactivated"));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
