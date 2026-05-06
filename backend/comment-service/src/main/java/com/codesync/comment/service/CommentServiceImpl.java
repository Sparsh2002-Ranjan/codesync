package com.codesync.comment.service;

import com.codesync.comment.client.NotificationClient;
import com.codesync.comment.entity.Comment;
import com.codesync.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public Comment addComment(Comment comment) {
        Comment saved = commentRepository.save(comment);

        // Fire notification (async, fire-and-forget)
        // In a real system, we'd look up the file owner from file-service
        log.debug("Comment added: {} on file {} line {}", saved.getCommentId(),
                saved.getFileId(), saved.getLineNumber());

        return saved;
    }

    @Override
    public Comment getCommentById(String commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
    }

    @Override
    public List<Comment> getByFile(String fileId) {
        return commentRepository.findByFileId(fileId);
    }

    @Override
    public List<Comment> getByProject(String projectId) {
        return commentRepository.findByProjectId(projectId);
    }

    @Override
    public List<Comment> getReplies(String parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId);
    }

    @Override
    public List<Comment> getByLine(String fileId, int lineNumber) {
        return commentRepository.findByFileIdAndLineNumber(fileId, lineNumber);
    }

    @Override
    @Transactional
    public Comment updateComment(String commentId, String newContent) {
        Comment comment = getCommentById(commentId);
        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public Comment resolveComment(String commentId) {
        Comment c = getCommentById(commentId);
        c.setResolved(true);
        return commentRepository.save(c);
    }

    @Override
    @Transactional
    public Comment unresolveComment(String commentId) {
        Comment c = getCommentById(commentId);
        c.setResolved(false);
        return commentRepository.save(c);
    }

    @Override
    public long getCommentCount(String fileId) {
        return commentRepository.countByFileId(fileId);
    }
}
