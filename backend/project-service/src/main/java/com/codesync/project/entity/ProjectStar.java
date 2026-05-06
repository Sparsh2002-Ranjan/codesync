package com.codesync.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_stars",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String starId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @CreationTimestamp
    @Column(name = "starred_at", updatable = false)
    private LocalDateTime starredAt;
}
