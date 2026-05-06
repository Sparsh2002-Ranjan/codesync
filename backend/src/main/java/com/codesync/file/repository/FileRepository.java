package com.codesync.file.repository;

import com.codesync.file.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<CodeFile, String> {

    List<CodeFile> findByProjectIdAndIsDeletedFalse(String projectId);

    Optional<CodeFile> findByProjectIdAndPathAndIsDeletedFalse(String projectId, String path);

    List<CodeFile> findByLanguageAndIsDeletedFalse(String language);

    List<CodeFile> findByLastEditedBy(String userId);

    long countByProjectId(String projectId);

    List<CodeFile> findByProjectIdAndIsDeletedTrue(String projectId);

    @Query("SELECT f FROM CodeFile f WHERE f.projectId = :projectId AND f.isDeleted = false AND (f.content LIKE %:query% OR f.name LIKE %:query%)")
    List<CodeFile> searchInProject(@Param("projectId") String projectId, @Param("query") String query);
}
