package com.codesync.collab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "collab_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CollabSession {

    @Id
    @Column(name = "session_id", updatable = false, nullable = false, length = 36)
    private String sessionId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "language", length = 50)
    private String language;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "max_participants")
    private int maxParticipants = 10;

    @Column(name = "is_password_protected")
    private boolean isPasswordProtected = false;

    /** Stored as BCrypt hash — never serialized to JSON */
    @JsonIgnore
    @Column(name = "session_password")
    private String sessionPassword;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    public enum SessionStatus {
        ACTIVE, ENDED
    }
}
