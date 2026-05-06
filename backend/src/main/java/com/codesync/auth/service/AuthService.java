package com.codesync.auth.service;

import com.codesync.auth.dto.AuthDTOs.*;
import com.codesync.auth.entity.User;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);

    boolean validateToken(String token);

    String refreshToken(String token);

    User getUserByEmail(String email);

    User getUserById(String userId);

    User updateProfile(String userId, UpdateProfileRequest request);

    void changePassword(String userId, ChangePasswordRequest request);

    List<User> searchUsers(String query);

    void deactivateAccount(String userId);

    List<User> getAllUsers();

    void activateAccount(String userId);

    void deleteUser(String userId);
}
