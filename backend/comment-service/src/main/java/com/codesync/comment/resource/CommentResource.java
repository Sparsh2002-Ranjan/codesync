package com.codesync.comment.resource;

import com.codesync.comment.entity.Comment;
import com.codesync.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/v1/comments") @RequiredArgsConstructor
public class CommentResource {
    private final CommentService commentService;

    @PostMapping public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(comment));
    }
    @GetMapping("/{commentId}") public ResponseEntity<Comment> getById(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }
    @GetMapping("/file/{fileId}") public ResponseEntity<List<Comment>> getByFile(@PathVariable String fileId) {
        return ResponseEntity.ok(commentService.getByFile(fileId));
    }
    @GetMapping("/project/{projectId}") public ResponseEntity<List<Comment>> getByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(commentService.getByProject(projectId));
    }
    @GetMapping("/{commentId}/replies") public ResponseEntity<List<Comment>> getReplies(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }
    @GetMapping("/file/{fileId}/line/{lineNumber}") public ResponseEntity<List<Comment>> getByLine(@PathVariable String fileId, @PathVariable int lineNumber) {
        return ResponseEntity.ok(commentService.getByLine(fileId, lineNumber));
    }
    @PutMapping("/{commentId}") public ResponseEntity<Comment> update(@PathVariable String commentId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(commentService.updateComment(commentId, body.get("content")));
    }
    @DeleteMapping("/{commentId}") public ResponseEntity<Map<String, String>> delete(@PathVariable String commentId) {
        commentService.deleteComment(commentId); return ResponseEntity.ok(Map.of("message", "Comment deleted"));
    }
    @PutMapping("/{commentId}/resolve") public ResponseEntity<Comment> resolve(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.resolveComment(commentId));
    }
    @PutMapping("/{commentId}/unresolve") public ResponseEntity<Comment> unresolve(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.unresolveComment(commentId));
    }
    @GetMapping("/file/{fileId}/count") public ResponseEntity<Map<String, Long>> getCount(@PathVariable String fileId) {
        return ResponseEntity.ok(Map.of("count", commentService.getCommentCount(fileId)));
    }
}
