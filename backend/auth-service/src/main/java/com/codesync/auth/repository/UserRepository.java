package com.codesync.auth.repository;

import com.codesync.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:query% OR u.fullName LIKE %:query%")
    List<User> searchByUsername(@Param("query") String query);

    void deleteByUserId(String userId);

    List<User> findByActive(boolean active);
}
