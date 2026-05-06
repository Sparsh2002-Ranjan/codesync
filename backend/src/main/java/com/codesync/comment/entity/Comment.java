package com.codesync.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comment_id", updatable = false, nullable = false)
    private String commentId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @Column(name = "column_number")
    private int columnNumber;

    @Column(name = "parent_comment_id")
    private String parentCommentId;     // null = top-level, set = reply

    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    @Column(name = "snapshot_id")
    private String snapshotId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
