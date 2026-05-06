package com.codesync.version.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "branches", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "name"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Branch {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id") private String branchId;
    @Column(name = "project_id", nullable = false) private String projectId;
    @Column(name = "name", nullable = false, length = 100) private String name;
    @Column(name = "head_snapshot_id") private String headSnapshotId;
    @Column(name = "created_by_id", nullable = false) private String createdById;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
