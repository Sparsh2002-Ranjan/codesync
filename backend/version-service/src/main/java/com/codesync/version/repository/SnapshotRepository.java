package com.codesync.version.repository;

import com.codesync.version.entity.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, String> {
    List<Snapshot> findByFileIdOrderByCreatedAtDesc(String fileId);
    List<Snapshot> findByProjectIdOrderByCreatedAtDesc(String projectId);
    List<Snapshot> findByAuthorId(String authorId);
    List<Snapshot> findByBranchAndFileId(String branch, String fileId);
    Optional<Snapshot> findByHash(String hash);
    Optional<Snapshot> findByTag(String tag);
    Optional<Snapshot> findFirstByFileIdAndBranchOrderByCreatedAtDesc(String fileId, String branch);
}
