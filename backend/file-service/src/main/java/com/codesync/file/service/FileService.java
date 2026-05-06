package com.codesync.file.service;

import com.codesync.file.entity.CodeFile;
import java.util.List;

public interface FileService {
    CodeFile createFile(CodeFile file);
    CodeFile createFolder(String projectId, String name, String path, String createdById);
    CodeFile getFileById(String fileId);
    List<CodeFile> getFilesByProject(String projectId);
    String getFileContent(String fileId);
    CodeFile updateFileContent(String fileId, String content, String editorUserId);
    CodeFile renameFile(String fileId, String newName);
    CodeFile moveFile(String fileId, String newPath);
    void deleteFile(String fileId);
    void restoreFile(String fileId);
    List<CodeFile> getFileTree(String projectId);
    List<CodeFile> searchInProject(String projectId, String query);
}
