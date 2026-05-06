package com.codesync.auth.service;

import com.codesync.auth.dto.AuthDTOs.*;
import com.codesync.auth.entity.User;
import com.codesync.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .bio(request.getBio())
                .role(User.Role.DEVELOPER)
                .provider(User.Provider.LOCAL)
                .active(true)
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getUserId(), user.getUsername(),
                user.getEmail(), user.getFullName(), user.getRole().name(), user.getAvatarUrl());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is suspended. Contact admin.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getUserId(), user.getUsername(),
                user.getEmail(), user.getFullName(), user.getRole().name(), user.getAvatarUrl());
    }

    @Override
    public void logout(String token) {
        // In production: add token to a blacklist (Redis)
        // For now: client deletes token
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) throw new RuntimeException("Invalid token");
        String userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        return jwtUtil.generateToken(userId, email, role);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Override
    @Transactional
    public User updateProfile(String userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        if (request.getFullName() != null)  user.setFullName(request.getFullName());
        if (request.getBio() != null)        user.setBio(request.getBio());
        if (request.getAvatarUrl() != null)  user.setAvatarUrl(request.getAvatarUrl());
        if (request.getUsername() != null) {
            if (userRepository.existsByUsername(request.getUsername())
                    && !user.getUsername().equals(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<User> searchUsers(String query) {
        return userRepository.searchByUsername(query);
    }

    @Override
    @Transactional
    public void deactivateAccount(String userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateAccount(String userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        userRepository.deleteByUserId(userId);
    }
}
