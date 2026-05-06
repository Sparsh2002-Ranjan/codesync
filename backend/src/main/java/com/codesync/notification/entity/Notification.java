package com.codesync.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", updatable = false, nullable = false)
    private String notificationId;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "related_id")
    private String relatedId;       // sessionId / commentId / snapshotId

    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "deep_link_url", length = 500)
    private String deepLinkUrl;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        SESSION_INVITE, PARTICIPANT_JOINED, PARTICIPANT_LEFT,
        COMMENT, MENTION, SNAPSHOT, FORK, BROADCAST
    }
}
