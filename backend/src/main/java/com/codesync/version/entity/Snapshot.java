package com.codesync.version.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "snapshots")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "snapshot_id", updatable = false, nullable = false)
    private String snapshotId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "message", nullable = false, length = 300)
    private String message;

    @Column(name = "content", columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;          // SHA-256 of content

    @Column(name = "parent_snapshot_id")
    private String parentSnapshotId;

    @Column(name = "branch", nullable = false, length = 100)
    private String branch = "main";

    @Column(name = "tag", length = 50)
    private String tag;           // e.g. "v1.0.0"

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
