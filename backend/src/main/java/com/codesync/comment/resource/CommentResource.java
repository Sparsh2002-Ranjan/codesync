package com.codesync.comment.resource;

import com.codesync.comment.entity.Comment;
import com.codesync.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CommentResource {

    private final CommentService commentService;

    // POST /api/v1/comments
    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(comment));
    }

    // GET /api/v1/comments/{commentId}
    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getById(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    // GET /api/v1/comments/file/{fileId}
    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<Comment>> getByFile(@PathVariable String fileId) {
        return ResponseEntity.ok(commentService.getByFile(fileId));
    }

    // GET /api/v1/comments/project/{projectId}
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Comment>> getByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(commentService.getByProject(projectId));
    }

    // GET /api/v1/comments/{commentId}/replies
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<Comment>> getReplies(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    // GET /api/v1/comments/file/{fileId}/line/{lineNumber}
    @GetMapping("/file/{fileId}/line/{lineNumber}")
    public ResponseEntity<List<Comment>> getByLine(@PathVariable String fileId,
                                                    @PathVariable int lineNumber) {
        return ResponseEntity.ok(commentService.getByLine(fileId, lineNumber));
    }

    // PUT /api/v1/comments/{commentId}
    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> update(@PathVariable String commentId,
                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(commentService.updateComment(commentId, body.get("content")));
    }

    // DELETE /api/v1/comments/{commentId}
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message", "Comment deleted"));
    }

    // PUT /api/v1/comments/{commentId}/resolve
    @PutMapping("/{commentId}/resolve")
    public ResponseEntity<Comment> resolve(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.resolveComment(commentId));
    }

    // PUT /api/v1/comments/{commentId}/unresolve
    @PutMapping("/{commentId}/unresolve")
    public ResponseEntity<Comment> unresolve(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.unresolveComment(commentId));
    }

    // GET /api/v1/comments/file/{fileId}/count
    @GetMapping("/file/{fileId}/count")
    public ResponseEntity<Map<String, Long>> getCount(@PathVariable String fileId) {
        return ResponseEntity.ok(Map.of("count", commentService.getCommentCount(fileId)));
    }
}
