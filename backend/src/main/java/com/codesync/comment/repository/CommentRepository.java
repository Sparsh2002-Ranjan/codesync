package com.codesync.comment.repository;

import com.codesync.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByFileId(String fileId);
    List<Comment> findByProjectId(String projectId);
    List<Comment> findByAuthorId(String authorId);
    List<Comment> findByFileIdAndLineNumber(String fileId, int lineNumber);
    List<Comment> findByParentCommentId(String parentCommentId);
    List<Comment> findByFileIdAndParentCommentIdIsNull(String fileId);   // top-level only
    List<Comment> findByResolved(boolean resolved);
    long countByFileId(String fileId);
}
