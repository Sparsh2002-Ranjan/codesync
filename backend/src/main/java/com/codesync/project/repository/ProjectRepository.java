package com.codesync.project.repository;

import com.codesync.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByOwnerId(String ownerId);

    List<Project> findByVisibility(Project.Visibility visibility);

    List<Project> findByLanguage(String language);

    @Query("SELECT p FROM Project p WHERE p.name LIKE %:q% OR p.description LIKE %:q%")
    List<Project> searchByName(@Param("q") String query);

    @Query("SELECT p FROM Project p JOIN ProjectMember m ON p.projectId = m.projectId WHERE m.userId = :userId")
    List<Project> findByMemberUserId(@Param("userId") String userId);

    List<Project> findByIsArchived(boolean archived);

    long countByOwnerId(String ownerId);
}
