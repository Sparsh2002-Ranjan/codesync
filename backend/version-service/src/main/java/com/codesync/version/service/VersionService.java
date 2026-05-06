package com.codesync.version.service;

import com.codesync.version.entity.Branch;
import com.codesync.version.entity.Snapshot;
import java.util.List;

public interface VersionService {
    Snapshot createSnapshot(Snapshot snapshot);
    Snapshot getSnapshotById(String snapshotId);
    List<Snapshot> getSnapshotsByFile(String fileId);
    List<Snapshot> getSnapshotsByProject(String projectId);
    List<Snapshot> getSnapshotsByBranch(String fileId, String branch);
    Snapshot getLatestSnapshot(String fileId, String branch);
    Snapshot restoreSnapshot(String snapshotId);
    List<String> diffSnapshots(String snapshotId1, String snapshotId2);
    Branch createBranch(String projectId, String name, String createdById);
    List<Branch> getBranches(String projectId);
    Snapshot tagSnapshot(String snapshotId, String tag);
    List<Snapshot> getFileHistory(String fileId);
}
