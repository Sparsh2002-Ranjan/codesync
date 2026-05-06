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
@CrossOrigin(origins = "http://localhost:4200")
public class FileResource {

    private final FileService fileService;

    // POST /api/v1/files
    @PostMapping
    public ResponseEntity<CodeFile> createFile(@RequestBody CodeFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.createFile(file));
    }

    // POST /api/v1/files/folder
    @PostMapping("/folder")
    public ResponseEntity<CodeFile> createFolder(@RequestParam String projectId,
                                                   @RequestParam String name,
                                                   @RequestParam String path,
                                                   @RequestParam String createdById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.createFolder(projectId, name, path, createdById));
    }

    // GET /api/v1/files/{fileId}
    @GetMapping("/{fileId}")
    public ResponseEntity<CodeFile> getById(@PathVariable String fileId) {
        return ResponseEntity.ok(fileService.getFileById(fileId));
    }

    // GET /api/v1/files/project/{projectId}
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CodeFile>> getByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(fileService.getFilesByProject(projectId));
    }

    // GET /api/v1/files/{fileId}/content
    @GetMapping("/{fileId}/content")
    public ResponseEntity<Map<String, String>> getContent(@PathVariable String fileId) {
        return ResponseEntity.ok(Map.of("content", fileService.getFileContent(fileId)));
    }

    // PUT /api/v1/files/{fileId}/content
    @PutMapping("/{fileId}/content")
    public ResponseEntity<CodeFile> updateContent(@PathVariable String fileId,
                                                   @RequestParam String editorUserId,
                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(fileService.updateFileContent(fileId, body.get("content"), editorUserId));
    }

    // PUT /api/v1/files/{fileId}/rename
    @PutMapping("/{fileId}/rename")
    public ResponseEntity<CodeFile> rename(@PathVariable String fileId,
                                            @RequestParam String newName) {
        return ResponseEntity.ok(fileService.renameFile(fileId, newName));
    }

    // PUT /api/v1/files/{fileId}/move
    @PutMapping("/{fileId}/move")
    public ResponseEntity<CodeFile> move(@PathVariable String fileId,
                                          @RequestParam String newPath) {
        return ResponseEntity.ok(fileService.moveFile(fileId, newPath));
    }

    // DELETE /api/v1/files/{fileId}
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(Map.of("message", "File deleted"));
    }

    // POST /api/v1/files/{fileId}/restore
    @PostMapping("/{fileId}/restore")
    public ResponseEntity<Map<String, String>> restore(@PathVariable String fileId) {
        fileService.restoreFile(fileId);
        return ResponseEntity.ok(Map.of("message", "File restored"));
    }

    // GET /api/v1/files/project/{projectId}/tree
    @GetMapping("/project/{projectId}/tree")
    public ResponseEntity<List<CodeFile>> getTree(@PathVariable String projectId) {
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    // GET /api/v1/files/project/{projectId}/search?query=
    @GetMapping("/project/{projectId}/search")
    public ResponseEntity<List<CodeFile>> search(@PathVariable String projectId,
                                                  @RequestParam String query) {
        return ResponseEntity.ok(fileService.searchInProject(projectId, query));
    }
}
