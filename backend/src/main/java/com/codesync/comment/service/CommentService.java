package com.codesync.comment.service;

import com.codesync.comment.entity.Comment;
import java.util.List;

public interface CommentService {
    Comment addComment(Comment comment);
    Comment getCommentById(String commentId);
    List<Comment> getByFile(String fileId);
    List<Comment> getByProject(String projectId);
    List<Comment> getReplies(String parentCommentId);
    List<Comment> getByLine(String fileId, int lineNumber);
    Comment updateComment(String commentId, String newContent);
    void deleteComment(String commentId);
    Comment resolveComment(String commentId);
    Comment unresolveComment(String commentId);
    long getCommentCount(String fileId);
}
