package com.codesync.comment.service;

import com.codesync.comment.entity.Comment;
import com.codesync.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
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
        Comment comment = getCommentById(commentId);
        comment.setResolved(true);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment unresolveComment(String commentId) {
        Comment comment = getCommentById(commentId);
        comment.setResolved(false);
        return commentRepository.save(comment);
    }

    @Override
    public long getCommentCount(String fileId) {
        return commentRepository.countByFileId(fileId);
    }
}
