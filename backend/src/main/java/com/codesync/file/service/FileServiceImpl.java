package com.codesync.file.service;

import com.codesync.file.entity.CodeFile;
import com.codesync.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    @Transactional
    public CodeFile createFile(CodeFile file) {
        file.setSize(file.getContent() != null ? file.getContent().length() : 0);
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public CodeFile createFolder(String projectId, String name, String path, String createdById) {
        CodeFile folder = CodeFile.builder()
                .projectId(projectId)
                .name(name)
                .path(path)
                .isFolder(true)
                .content("")
                .language("")
                .size(0)
                .createdById(createdById)
                .lastEditedBy(createdById)
                .build();
        return fileRepository.save(folder);
    }

    @Override
    public CodeFile getFileById(String fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));
    }

    @Override
    public List<CodeFile> getFilesByProject(String projectId) {
        return fileRepository.findByProjectIdAndIsDeletedFalse(projectId);
    }

    @Override
    public String getFileContent(String fileId) {
        return getFileById(fileId).getContent();
    }

    @Override
    @Transactional
    public CodeFile updateFileContent(String fileId, String content, String editorUserId) {
        CodeFile file = getFileById(fileId);
        file.setContent(content);
        file.setSize(content != null ? content.length() : 0);
        file.setLastEditedBy(editorUserId);
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public CodeFile renameFile(String fileId, String newName) {
        CodeFile file = getFileById(fileId);
        file.setName(newName);
        // Update path with new name
        String oldPath = file.getPath();
        String newPath = oldPath.contains("/")
                ? oldPath.substring(0, oldPath.lastIndexOf('/') + 1) + newName
                : newName;
        file.setPath(newPath);
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public CodeFile moveFile(String fileId, String newPath) {
        CodeFile file = getFileById(fileId);
        file.setPath(newPath);
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFile(String fileId) {
        CodeFile file = getFileById(fileId);
        file.setDeleted(true);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void restoreFile(String fileId) {
        CodeFile file = getFileById(fileId);
        file.setDeleted(false);
        fileRepository.save(file);
    }

    @Override
    public List<CodeFile> getFileTree(String projectId) {
        return fileRepository.findByProjectIdAndIsDeletedFalse(projectId);
    }

    @Override
    public List<CodeFile> searchInProject(String projectId, String query) {
        return fileRepository.searchInProject(projectId, query);
    }
}
