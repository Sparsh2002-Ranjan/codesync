package com.codesync.file.resource;

import com.codesync.file.entity.CodeFile;
import com.codesync.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileResource {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<CodeFile> createFile(@RequestBody CodeFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.createFile(file));
    }

    @PostMapping("/folder")
    public ResponseEntity<CodeFile> createFolder(@RequestParam String projectId,
                                                   @RequestParam String name,
                                                   @RequestParam String path,
                                                   @RequestParam String createdById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.createFolder(projectId, name, path, createdById));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<CodeFile> getById(@PathVariable String fileId) {
        return ResponseEntity.ok(fileService.getFileById(fileId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CodeFile>> getByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(fileService.getFilesByProject(projectId));
    }

    @GetMapping("/{fileId}/content")
    public ResponseEntity<Map<String, String>> getContent(@PathVariable String fileId) {
        return ResponseEntity.ok(Map.of("content", fileService.getFileContent(fileId)));
    }

    @PutMapping("/{fileId}/content")
    public ResponseEntity<CodeFile> updateContent(@PathVariable String fileId,
                                                   @RequestParam String editorUserId,
                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(fileService.updateFileContent(fileId, body.get("content"), editorUserId));
    }

    @PutMapping("/{fileId}/rename")
    public ResponseEntity<CodeFile> rename(@PathVariable String fileId, @RequestParam String newName) {
        return ResponseEntity.ok(fileService.renameFile(fileId, newName));
    }

    @PutMapping("/{fileId}/move")
    public ResponseEntity<CodeFile> move(@PathVariable String fileId, @RequestParam String newPath) {
        return ResponseEntity.ok(fileService.moveFile(fileId, newPath));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(Map.of("message", "File deleted"));
    }

    @PostMapping("/{fileId}/restore")
    public ResponseEntity<Map<String, String>> restore(@PathVariable String fileId) {
        fileService.restoreFile(fileId);
        return ResponseEntity.ok(Map.of("message", "File restored"));
    }

    @GetMapping("/project/{projectId}/tree")
    public ResponseEntity<List<CodeFile>> getTree(@PathVariable String projectId) {
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/project/{projectId}/search")
    public ResponseEntity<List<CodeFile>> search(@PathVariable String projectId, @RequestParam String query) {
        return ResponseEntity.ok(fileService.searchInProject(projectId, query));
    }
}
