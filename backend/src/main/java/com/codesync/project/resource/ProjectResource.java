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
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectResource {

    private final ProjectService projectService;

    // POST /api/v1/projects
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(project));
    }

    // GET /api/v1/projects/{projectId}
    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getById(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    // GET /api/v1/projects/owner/{ownerId}
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Project>> getByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(ownerId));
    }

    // GET /api/v1/projects/public
    @GetMapping("/public")
    public ResponseEntity<List<Project>> getPublic() {
        return ResponseEntity.ok(projectService.getPublicProjects());
    }

    // GET /api/v1/projects/search?query=
    @GetMapping("/search")
    public ResponseEntity<List<Project>> search(@RequestParam String query) {
        return ResponseEntity.ok(projectService.searchProjects(query));
    }

    // GET /api/v1/projects/member/{userId}
    @GetMapping("/member/{userId}")
    public ResponseEntity<List<Project>> getByMember(@PathVariable String userId) {
        return ResponseEntity.ok(projectService.getProjectsByMember(userId));
    }

    // GET /api/v1/projects/language/{language}
    @GetMapping("/language/{language}")
    public ResponseEntity<List<Project>> getByLanguage(@PathVariable String language) {
        return ResponseEntity.ok(projectService.getProjectsByLanguage(language));
    }

    // PUT /api/v1/projects/{projectId}
    @PutMapping("/{projectId}")
    public ResponseEntity<Project> update(@PathVariable String projectId,
                                           @RequestBody Project project) {
        return ResponseEntity.ok(projectService.updateProject(projectId, project));
    }

    // PUT /api/v1/projects/{projectId}/archive
    @PutMapping("/{projectId}/archive")
    public ResponseEntity<Map<String, String>> archive(@PathVariable String projectId) {
        projectService.archiveProject(projectId);
        return ResponseEntity.ok(Map.of("message", "Project archived"));
    }

    // DELETE /api/v1/projects/{projectId}
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted"));
    }

    // POST /api/v1/projects/{projectId}/fork?newOwnerId=
    @PostMapping("/{projectId}/fork")
    public ResponseEntity<Project> fork(@PathVariable String projectId,
                                         @RequestParam String newOwnerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.forkProject(projectId, newOwnerId));
    }

    // POST /api/v1/projects/{projectId}/star?userId=
    @PostMapping("/{projectId}/star")
    public ResponseEntity<Map<String, String>> star(@PathVariable String projectId,
                                                     @RequestParam String userId) {
        projectService.starProject(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Project starred"));
    }

    // DELETE /api/v1/projects/{projectId}/star?userId=
    @DeleteMapping("/{projectId}/star")
    public ResponseEntity<Map<String, String>> unstar(@PathVariable String projectId,
                                                       @RequestParam String userId) {
        projectService.unstarProject(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Project unstarred"));
    }

    // GET /api/v1/projects/{projectId}/members
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMember>> getMembers(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getMembers(projectId));
    }

    // POST /api/v1/projects/{projectId}/members?userId=&role=
    @PostMapping("/{projectId}/members")
    public ResponseEntity<Map<String, String>> addMember(@PathVariable String projectId,
                                                          @RequestParam String userId,
                                                          @RequestParam(defaultValue = "EDITOR") String role) {
        projectService.addMember(projectId, userId, ProjectMember.MemberRole.valueOf(role));
        return ResponseEntity.ok(Map.of("message", "Member added"));
    }

    // DELETE /api/v1/projects/{projectId}/members/{userId}
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(@PathVariable String projectId,
                                                             @PathVariable String userId) {
        projectService.removeMember(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Member removed"));
    }
}
