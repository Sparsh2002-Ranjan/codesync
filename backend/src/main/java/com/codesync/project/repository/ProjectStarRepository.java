package com.codesync.project.repository;

import com.codesync.project.entity.ProjectStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectStarRepository extends JpaRepository<ProjectStar, String> {
    boolean existsByProjectIdAndUserId(String projectId, String userId);
    Optional<ProjectStar> findByProjectIdAndUserId(String projectId, String userId);
    long countByProjectId(String projectId);
}
