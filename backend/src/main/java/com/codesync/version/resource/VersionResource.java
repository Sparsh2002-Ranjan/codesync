package com.codesync.version.resource;

import com.codesync.version.entity.Branch;
import com.codesync.version.entity.Snapshot;
import com.codesync.version.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VersionResource {

    private final VersionService versionService;

    // POST /api/v1/versions/snapshots
    @PostMapping("/snapshots")
    public ResponseEntity<Snapshot> createSnapshot(@RequestBody Snapshot snapshot) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(versionService.createSnapshot(snapshot));
    }

    // GET /api/v1/versions/snapshots/{snapshotId}
    @GetMapping("/snapshots/{snapshotId}")
    public ResponseEntity<Snapshot> getById(@PathVariable String snapshotId) {
        return ResponseEntity.ok(versionService.getSnapshotById(snapshotId));
    }

    // GET /api/v1/versions/snapshots/file/{fileId}
    @GetMapping("/snapshots/file/{fileId}")
    public ResponseEntity<List<Snapshot>> getByFile(@PathVariable String fileId) {
        return ResponseEntity.ok(versionService.getSnapshotsByFile(fileId));
    }

    // GET /api/v1/versions/snapshots/project/{projectId}
    @GetMapping("/snapshots/project/{projectId}")
    public ResponseEntity<List<Snapshot>> getByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(versionService.getSnapshotsByProject(projectId));
    }

    // GET /api/v1/versions/snapshots/file/{fileId}/branch/{branch}
    @GetMapping("/snapshots/file/{fileId}/branch/{branch}")
    public ResponseEntity<List<Snapshot>> getByBranch(@PathVariable String fileId,
                                                        @PathVariable String branch) {
        return ResponseEntity.ok(versionService.getSnapshotsByBranch(fileId, branch));
    }

    // GET /api/v1/versions/snapshots/file/{fileId}/latest?branch=main
    @GetMapping("/snapshots/file/{fileId}/latest")
    public ResponseEntity<Snapshot> getLatest(@PathVariable String fileId,
                                               @RequestParam(defaultValue = "main") String branch) {
        return ResponseEntity.ok(versionService.getLatestSnapshot(fileId, branch));
    }

    // POST /api/v1/versions/snapshots/{snapshotId}/restore
    @PostMapping("/snapshots/{snapshotId}/restore")
    public ResponseEntity<Snapshot> restore(@PathVariable String snapshotId) {
        return ResponseEntity.ok(versionService.restoreSnapshot(snapshotId));
    }

    // GET /api/v1/versions/snapshots/diff?s1=&s2=
    @GetMapping("/snapshots/diff")
    public ResponseEntity<List<String>> diff(@RequestParam String s1, @RequestParam String s2) {
        return ResponseEntity.ok(versionService.diffSnapshots(s1, s2));
    }

    // GET /api/v1/versions/history/{fileId}
    @GetMapping("/history/{fileId}")
    public ResponseEntity<List<Snapshot>> getHistory(@PathVariable String fileId) {
        return ResponseEntity.ok(versionService.getFileHistory(fileId));
    }

    // POST /api/v1/versions/branches?projectId=&name=&createdById=
    @PostMapping("/branches")
    public ResponseEntity<Branch> createBranch(@RequestParam String projectId,
                                                @RequestParam String name,
                                                @RequestParam String createdById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(versionService.createBranch(projectId, name, createdById));
    }

    // GET /api/v1/versions/branches/{projectId}
    @GetMapping("/branches/{projectId}")
    public ResponseEntity<List<Branch>> getBranches(@PathVariable String projectId) {
        return ResponseEntity.ok(versionService.getBranches(projectId));
    }

    // PUT /api/v1/versions/snapshots/{snapshotId}/tag?tag=v1.0.0
    @PutMapping("/snapshots/{snapshotId}/tag")
    public ResponseEntity<Snapshot> tagSnapshot(@PathVariable String snapshotId,
                                                 @RequestParam String tag) {
        return ResponseEntity.ok(versionService.tagSnapshot(snapshotId, tag));
    }
}
