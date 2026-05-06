package com.codesync.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_id", updatable = false, nullable = false)
    private String projectId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "language", nullable = false, length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "star_count", nullable = false)
    private int starCount = 0;

    @Column(name = "fork_count", nullable = false)
    private int forkCount = 0;

    @Column(name = "forked_from_id")
    private String forkedFromId;

    public enum Visibility {
        PUBLIC, PRIVATE
    }
}
