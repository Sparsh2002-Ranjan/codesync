package com.codesync.auth.resource;

import com.codesync.auth.dto.AuthDTOs.*;
import com.codesync.auth.entity.User;
import com.codesync.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // POST /api/v1/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String newToken = authService.refreshToken(token);
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    // GET /api/v1/auth/validate
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        boolean valid = authService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    // GET /api/v1/auth/profile/{userId}
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable String userId) {
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(toUserResponse(user));
    }

    // PUT /api/v1/auth/profile/{userId}
    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable String userId,
                                                       @RequestBody UpdateProfileRequest request) {
        User updated = authService.updateProfile(userId, request);
        return ResponseEntity.ok(toUserResponse(updated));
    }

    // PUT /api/v1/auth/password/{userId}
    @PutMapping("/password/{userId}")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable String userId,
                                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // GET /api/v1/auth/search?query=
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        List<UserResponse> users = authService.searchUsers(query)
                .stream().map(this::toUserResponse).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // GET /api/v1/auth/users  — ADMIN only (enforced in SecurityConfig)
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = authService.getAllUsers()
                .stream().map(this::toUserResponse).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // PUT /api/v1/auth/deactivate/{userId}  — ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{userId}")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable String userId) {
        authService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Account deactivated"));
    }

    // PUT /api/v1/auth/activate/{userId}  — ADMIN only (also in SecurityConfig)
    @PutMapping("/activate/{userId}")
    public ResponseEntity<Map<String, String>> activate(@PathVariable String userId) {
        authService.activateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Account activated"));
    }

    // DELETE /api/v1/auth/users/{userId}  — ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    private UserResponse toUserResponse(User u) {
        UserResponse r = new UserResponse();
        r.setUserId(u.getUserId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setFullName(u.getFullName());
        r.setRole(u.getRole().name());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setProvider(u.getProvider().name());
        r.setActive(u.isActive());
        r.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
        r.setBio(u.getBio());
        return r;
    }
}
