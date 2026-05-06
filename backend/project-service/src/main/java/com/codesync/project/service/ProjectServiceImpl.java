package com.codesync.project.service;

import com.codesync.project.client.NotificationClient;
import com.codesync.project.entity.Project;
import com.codesync.project.entity.ProjectMember;
import com.codesync.project.entity.ProjectStar;
import com.codesync.project.repository.ProjectMemberRepository;
import com.codesync.project.repository.ProjectRepository;
import com.codesync.project.repository.ProjectStarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectStarRepository starRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public Project createProject(Project project) {
        Project saved = projectRepository.save(project);
        ProjectMember ownerMember = ProjectMember.builder()
                .projectId(saved.getProjectId())
                .userId(saved.getOwnerId())
                .memberRole(ProjectMember.MemberRole.OWNER)
                .build();
        memberRepository.save(ownerMember);
        return saved;
    }

    @Override
    public Project getProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
    }

    @Override
    public List<Project> getProjectsByOwner(String ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Project> getPublicProjects() {
        return projectRepository.findByVisibility(Project.Visibility.PUBLIC);
    }

    @Override
    public List<Project> searchProjects(String query) {
        return projectRepository.searchByName(query);
    }

    @Override
    public List<Project> getProjectsByMember(String userId) {
        return projectRepository.findByMemberUserId(userId);
    }

    @Override
    public List<Project> getProjectsByLanguage(String language) {
        return projectRepository.findByLanguage(language);
    }

    @Override
    @Transactional
    public Project updateProject(String projectId, Project updated) {
        Project existing = getProjectById(projectId);
        if (updated.getName() != null)        existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getLanguage() != null)    existing.setLanguage(updated.getLanguage());
        if (updated.getVisibility() != null)  existing.setVisibility(updated.getVisibility());
        return projectRepository.save(existing);
    }

    @Override
    @Transactional
    public void archiveProject(String projectId) {
        Project p = getProjectById(projectId);
        p.setArchived(true);
        projectRepository.save(p);
    }

    @Override
    @Transactional
    public void deleteProject(String projectId) {
        projectRepository.deleteById(projectId);
    }

    @Override
    @Transactional
    public Project forkProject(String sourceProjectId, String newOwnerId) {
        Project source = getProjectById(sourceProjectId);
        Project fork = Project.builder()
                .ownerId(newOwnerId)
                .name(source.getName() + "-fork")
                .description("Forked from " + source.getName())
                .language(source.getLanguage())
                .visibility(Project.Visibility.PRIVATE)
                .forkedFromId(sourceProjectId)
                .starCount(0)
                .forkCount(0)
                .build();
        source.setForkCount(source.getForkCount() + 1);
        projectRepository.save(source);
        Project saved = createProject(fork);

        // Notify project owner about the fork
        try {
            notificationClient.notifyFork(source.getOwnerId(), newOwnerId,
                    sourceProjectId, source.getName());
        } catch (Exception e) {
            log.warn("Failed to send fork notification: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional
    public void starProject(String projectId, String userId) {
        if (starRepository.existsByProjectIdAndUserId(projectId, userId)) return;
        starRepository.save(ProjectStar.builder().projectId(projectId).userId(userId).build());
        Project p = getProjectById(projectId);
        p.setStarCount(p.getStarCount() + 1);
        projectRepository.save(p);

        // Notify project owner about the star
        try {
            if (!p.getOwnerId().equals(userId)) {
                notificationClient.notifyStar(p.getOwnerId(), userId, projectId, p.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to send star notification: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void unstarProject(String projectId, String userId) {
        starRepository.findByProjectIdAndUserId(projectId, userId)
                .ifPresent(star -> {
                    starRepository.delete(star);
                    Project p = getProjectById(projectId);
                    p.setStarCount(Math.max(0, p.getStarCount() - 1));
                    projectRepository.save(p);
                });
    }

    @Override
    public boolean isStarred(String projectId, String userId) {
        return starRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public List<Project> getStarredProjectsByUser(String userId) {
        return starRepository.findByUserId(userId).stream()
                .map(star -> projectRepository.findById(star.getProjectId()).orElse(null))
                .filter(p -> p != null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(String projectId, String userId, ProjectMember.MemberRole role) {
        if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) return;
        memberRepository.save(ProjectMember.builder()
                .projectId(projectId).userId(userId).memberRole(role).build());

        // Notify the new member
        try {
            Project p = getProjectById(projectId);
            notificationClient.notifyMemberAdded(userId, p.getOwnerId(), projectId, p.getName());
        } catch (Exception e) {
            log.warn("Failed to send member-added notification: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeMember(String projectId, String userId) {
        memberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public List<ProjectMember> getMembers(String projectId) {
        return memberRepository.findByProjectId(projectId);
    }
}
