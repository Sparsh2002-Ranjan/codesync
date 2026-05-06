package com.codesync.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ── Register Request ────────────────────────────
public class AuthDTOs {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String fullName;
        private String bio;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String userId;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String avatarUrl;

        public AuthResponse(String token, String userId, String username,
                            String email, String fullName, String role, String avatarUrl) {
            this.token = token;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.avatarUrl = avatarUrl;
        }
    }

    @Data
    public static class UpdateProfileRequest {
        private String fullName;
        private String bio;
        private String avatarUrl;
        private String username;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        @Size(min = 6)
        private String newPassword;
    }

    @Data
    public static class UserResponse {
        private String userId;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String avatarUrl;
        private String provider;
        private boolean isActive;
        private String createdAt;
        private String bio;
    }
}
