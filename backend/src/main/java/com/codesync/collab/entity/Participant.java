package com.codesync.collab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_participants")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "participant_id")
    private String participantId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ParticipantRole role = ParticipantRole.EDITOR;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "cursor_line")
    private int cursorLine = 1;

    @Column(name = "cursor_col")
    private int cursorCol = 1;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "is_active")
    private boolean isActive = true;

    public enum ParticipantRole {
        HOST, EDITOR, VIEWER
    }
}
