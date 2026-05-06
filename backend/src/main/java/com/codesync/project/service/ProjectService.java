package com.codesync.project.service;

import com.codesync.project.entity.Project;
import com.codesync.project.entity.ProjectMember;
import java.util.List;

public interface ProjectService {
    Project createProject(Project project);
    Project getProjectById(String projectId);
    List<Project> getProjectsByOwner(String ownerId);
    List<Project> getPublicProjects();
    List<Project> searchProjects(String query);
    List<Project> getProjectsByMember(String userId);
    List<Project> getProjectsByLanguage(String language);
    Project updateProject(String projectId, Project project);
    void archiveProject(String projectId);
    void deleteProject(String projectId);
    Project forkProject(String projectId, String newOwnerId);
    void starProject(String projectId, String userId);
    void unstarProject(String projectId, String userId);
    boolean isStarred(String projectId, String userId);
    void addMember(String projectId, String userId, ProjectMember.MemberRole role);
    void removeMember(String projectId, String userId);
    List<ProjectMember> getMembers(String projectId);
}
