package com.codesync.web.controller;

import com.codesync.auth.entity.User;
import com.codesync.auth.service.AuthService;
import com.codesync.collab.service.CollabService;
import com.codesync.notification.service.NotificationService;
import com.codesync.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})
public class AdminController {

    private final AuthService authService;
    private final ProjectService projectService;
    private final CollabService collabService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(Map.of(
            "totalUsers",     authService.getAllUsers().size(),
            "totalProjects",  projectService.getPublicProjects().size(),
            "activeSessions", collabService.getActiveSessionsAll().size(),
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

    @GetMapping("/sessions")
    public ResponseEntity<?> getActiveSessions() {
        return ResponseEntity.ok(collabService.getActiveSessionsAll());
    }

    @PostMapping("/sessions/{sessionId}/terminate")
    public ResponseEntity<Map<String, String>> terminateSession(@PathVariable String sessionId) {
        collabService.endSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session terminated"));
    }

    @PostMapping("/notifications/broadcast")
    public ResponseEntity<Map<String, String>> broadcast(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> recipientIds = (List<String>) body.get("recipientIds");
        notificationService.sendBulk(
            recipientIds != null ? recipientIds
                : authService.getAllUsers().stream().map(User::getUserId).toList(),
            com.codesync.notification.entity.Notification.NotificationType.BROADCAST,
            (String) body.get("title"),
            (String) body.get("message"),
            "admin"
        );
        return ResponseEntity.ok(Map.of("message", "Broadcast sent"));
    }
}
