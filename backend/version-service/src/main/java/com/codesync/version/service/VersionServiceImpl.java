package com.codesync.version.service;

import com.codesync.version.entity.Branch;
import com.codesync.version.entity.Snapshot;
import com.codesync.version.repository.BranchRepository;
import com.codesync.version.repository.SnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final SnapshotRepository snapshotRepository;
    private final BranchRepository branchRepository;

    @Override @Transactional
    public Snapshot createSnapshot(Snapshot snapshot) {
        snapshot.setHash(sha256(snapshot.getContent()));
        if (snapshot.getBranch() == null) snapshot.setBranch("main");
        snapshotRepository.findFirstByFileIdAndBranchOrderByCreatedAtDesc(snapshot.getFileId(), snapshot.getBranch())
                .ifPresent(head -> snapshot.setParentSnapshotId(head.getSnapshotId()));
        Snapshot saved = snapshotRepository.save(snapshot);
        branchRepository.findByProjectIdAndName(snapshot.getProjectId(), snapshot.getBranch())
                .ifPresent(branch -> { branch.setHeadSnapshotId(saved.getSnapshotId()); branchRepository.save(branch); });
        return saved;
    }

    @Override public Snapshot getSnapshotById(String snapshotId) {
        return snapshotRepository.findById(snapshotId).orElseThrow(() -> new RuntimeException("Snapshot not found: " + snapshotId));
    }

    @Override public List<Snapshot> getSnapshotsByFile(String fileId) { return snapshotRepository.findByFileIdOrderByCreatedAtDesc(fileId); }
    @Override public List<Snapshot> getSnapshotsByProject(String projectId) { return snapshotRepository.findByProjectIdOrderByCreatedAtDesc(projectId); }
    @Override public List<Snapshot> getSnapshotsByBranch(String fileId, String branch) { return snapshotRepository.findByBranchAndFileId(branch, fileId); }

    @Override public Snapshot getLatestSnapshot(String fileId, String branch) {
        return snapshotRepository.findFirstByFileIdAndBranchOrderByCreatedAtDesc(fileId, branch)
                .orElseThrow(() -> new RuntimeException("No snapshots found: " + fileId));
    }

    @Override @Transactional
    public Snapshot restoreSnapshot(String snapshotId) {
        Snapshot old = getSnapshotById(snapshotId);
        Snapshot restored = Snapshot.builder().projectId(old.getProjectId()).fileId(old.getFileId())
                .authorId(old.getAuthorId()).message("Restored from: " + old.getMessage())
                .content(old.getContent()).branch(old.getBranch()).build();
        return createSnapshot(restored);
    }

    @Override
    public List<String> diffSnapshots(String snapshotId1, String snapshotId2) {
        Snapshot s1 = getSnapshotById(snapshotId1);
        Snapshot s2 = getSnapshotById(snapshotId2);
        return computeDiff(s1.getContent(), s2.getContent(), "snapshot/" + snapshotId1, "snapshot/" + snapshotId2);
    }

    private List<String> computeDiff(String original, String revised, String fromLabel, String toLabel) {
        String[] oldLines = original.split("\n", -1);
        String[] newLines = revised.split("\n", -1);
        List<String> result = new ArrayList<>();
        result.add("--- " + fromLabel);
        result.add("+++ " + toLabel);
        int i = 0, j = 0;
        while (i < oldLines.length || j < newLines.length) {
            if (i < oldLines.length && j < newLines.length && oldLines[i].equals(newLines[j])) {
                result.add("  " + oldLines[i]); i++; j++;
            } else {
                int matchI = -1, matchJ = -1;
                outer: for (int di = 0; di <= 5; di++) {
                    for (int dj = 0; dj <= 5; dj++) {
                        if (i + di < oldLines.length && j + dj < newLines.length && oldLines[i + di].equals(newLines[j + dj])) {
                            matchI = i + di; matchJ = j + dj; break outer;
                        }
                    }
                }
                int endI = matchI >= 0 ? matchI : oldLines.length;
                int endJ = matchJ >= 0 ? matchJ : newLines.length;
                if (endI > i || endJ > j) result.add("@@ -" + (i+1) + "," + (endI-i) + " +" + (j+1) + "," + (endJ-j) + " @@");
                while (i < endI) { result.add("- " + oldLines[i++]); }
                while (j < endJ) { result.add("+ " + newLines[j++]); }
            }
        }
        return result;
    }

    @Override @Transactional
    public Branch createBranch(String projectId, String name, String createdById) {
        if (branchRepository.existsByProjectIdAndName(projectId, name)) throw new RuntimeException("Branch already exists: " + name);
        return branchRepository.save(Branch.builder().projectId(projectId).name(name).createdById(createdById).build());
    }

    @Override public List<Branch> getBranches(String projectId) { return branchRepository.findByProjectId(projectId); }

    @Override @Transactional
    public Snapshot tagSnapshot(String snapshotId, String tag) {
        Snapshot snapshot = getSnapshotById(snapshotId);
        snapshot.setTag(tag);
        return snapshotRepository.save(snapshot);
    }

    @Override public List<Snapshot> getFileHistory(String fileId) { return snapshotRepository.findByFileIdOrderByCreatedAtDesc(fileId); }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) { return Integer.toHexString(content.hashCode()); }
    }
}
