package com.codesync.auth.resource;

import com.codesync.auth.dto.AuthDTOs.*;
import com.codesync.auth.entity.User;
import com.codesync.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
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
    public ResponseEntity<User> getProfile(@PathVariable String userId) {
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // PUT /api/v1/auth/profile/{userId}
    @PutMapping("/profile/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable String userId,
                                               @RequestBody UpdateProfileRequest request) {
        User updated = authService.updateProfile(userId, request);
        return ResponseEntity.ok(updated);
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
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        List<User> users = authService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    // GET /api/v1/auth/users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    // PUT /api/v1/auth/deactivate/{userId}
    @PutMapping("/deactivate/{userId}")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable String userId) {
        authService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Account deactivated"));
    }

    // PUT /api/v1/auth/activate/{userId}
    @PutMapping("/activate/{userId}")
    public ResponseEntity<Map<String, String>> activate(@PathVariable String userId) {
        authService.activateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Account activated"));
    }

    // DELETE /api/v1/auth/users/{userId}
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
