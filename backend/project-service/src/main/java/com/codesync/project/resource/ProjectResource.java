package com.codesync.project.resource;

import com.codesync.project.entity.Project;
import com.codesync.project.entity.ProjectMember;
import com.codesync.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectResource {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(project));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getById(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Project>> getByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(ownerId));
    }

    @GetMapping("/public")
    public ResponseEntity<List<Project>> getPublic() {
        return ResponseEntity.ok(projectService.getPublicProjects());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Project>> search(@RequestParam String query) {
        return ResponseEntity.ok(projectService.searchProjects(query));
    }

    @GetMapping("/member/{userId}")
    public ResponseEntity<List<Project>> getByMember(@PathVariable String userId) {
        return ResponseEntity.ok(projectService.getProjectsByMember(userId));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<List<Project>> getByLanguage(@PathVariable String language) {
        return ResponseEntity.ok(projectService.getProjectsByLanguage(language));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> update(@PathVariable String projectId,
                                           @RequestBody Project project) {
        return ResponseEntity.ok(projectService.updateProject(projectId, project));
    }

    @PutMapping("/{projectId}/archive")
    public ResponseEntity<Map<String, String>> archive(@PathVariable String projectId) {
        projectService.archiveProject(projectId);
        return ResponseEntity.ok(Map.of("message", "Project archived"));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted"));
    }

    @PostMapping("/{projectId}/fork")
    public ResponseEntity<Project> fork(@PathVariable String projectId,
                                         @RequestParam String newOwnerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.forkProject(projectId, newOwnerId));
    }

    @PostMapping("/{projectId}/star")
    public ResponseEntity<Map<String, String>> star(@PathVariable String projectId,
                                                     @RequestParam String userId) {
        projectService.starProject(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Project starred"));
    }

    @DeleteMapping("/{projectId}/star")
    public ResponseEntity<Map<String, String>> unstar(@PathVariable String projectId,
                                                       @RequestParam String userId) {
        projectService.unstarProject(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Project unstarred"));
    }

    @GetMapping("/{projectId}/star")
    public ResponseEntity<Map<String, Boolean>> isStarred(@PathVariable String projectId,
                                                           @RequestParam String userId) {
        return ResponseEntity.ok(Map.of("starred", projectService.isStarred(projectId, userId)));
    }

    @GetMapping("/starred/{userId}")
    public ResponseEntity<List<Project>> getStarredByUser(@PathVariable String userId) {
        return ResponseEntity.ok(projectService.getStarredProjectsByUser(userId));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMember>> getMembers(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getMembers(projectId));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<Map<String, String>> addMember(@PathVariable String projectId,
                                                          @RequestParam String userId,
                                                          @RequestParam(defaultValue = "EDITOR") String role) {
        projectService.addMember(projectId, userId, ProjectMember.MemberRole.valueOf(role));
        return ResponseEntity.ok(Map.of("message", "Member added"));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(@PathVariable String projectId,
                                                             @PathVariable String userId) {
        projectService.removeMember(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Member removed"));
    }
}
