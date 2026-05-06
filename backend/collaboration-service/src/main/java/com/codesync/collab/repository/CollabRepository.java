package com.codesync.collab.repository;

import com.codesync.collab.entity.CollabSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CollabRepository extends JpaRepository<CollabSession, String> {
    List<CollabSession> findByProjectId(String projectId);
    List<CollabSession> findByFileId(String fileId);
    List<CollabSession> findByStatus(CollabSession.SessionStatus status);
    @Query("SELECT s FROM CollabSession s WHERE s.projectId = :pid AND s.status = 'ACTIVE'")
    List<CollabSession> findActiveByProjectId(@Param("pid") String projectId);
    List<CollabSession> findByOwnerId(String ownerId);
    @Query("SELECT s FROM CollabSession s WHERE s.status = 'ACTIVE' AND s.lastActivityAt < :cutoff")
    List<CollabSession> findInactiveSessions(@Param("cutoff") LocalDateTime cutoff);
}
