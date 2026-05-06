package com.codesync.file.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "file_id", updatable = false, nullable = false)
    private String fileId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "path", nullable = false, length = 500)
    private String path;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "size")
    private long size;

    @Column(name = "is_folder", nullable = false)
    private boolean isFolder = false;

    @Column(name = "parent_path", length = 500)
    private String parentPath;

    @Column(name = "created_by_id", nullable = false)
    private String createdById;

    @Column(name = "last_edited_by")
    private String lastEditedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
