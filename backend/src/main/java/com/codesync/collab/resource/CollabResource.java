package com.codesync.collab.resource;

import com.codesync.collab.entity.CollabSession;
import com.codesync.collab.entity.Participant;
import com.codesync.collab.service.CollabService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CollabResource {

    private final CollabService collabService;
    private final SimpMessagingTemplate messagingTemplate;

    // POST /api/v1/sessions
    @PostMapping
    public ResponseEntity<CollabSession> createSession(@RequestBody CollabSession session) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collabService.createSession(session));
    }

    // GET /api/v1/sessions/{sessionId}
    @GetMapping("/{sessionId}")
    public ResponseEntity<CollabSession> getById(@PathVariable String sessionId) {
        return ResponseEntity.ok(collabService.getSessionById(sessionId));
    }

    // GET /api/v1/sessions/project/{projectId}
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CollabSession>> getByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(collabService.getSessionsByProject(projectId));
    }

    // POST /api/v1/sessions/{sessionId}/join
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<Participant> join(@PathVariable String sessionId,
                                             @RequestParam String userId,
                                             @RequestParam(required = false) String password) {
        Participant p = collabService.joinSession(sessionId, userId, password);
        // Broadcast join event to all in session
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "PARTICIPANT_JOINED", "userId", userId));
        return ResponseEntity.ok(p);
    }

    // POST /api/v1/sessions/{sessionId}/leave
    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<Map<String, String>> leave(@PathVariable String sessionId,
                                                      @RequestParam String userId) {
        collabService.leaveSession(sessionId, userId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "PARTICIPANT_LEFT", "userId", userId));
        return ResponseEntity.ok(Map.of("message", "Left session"));
    }

    // POST /api/v1/sessions/{sessionId}/end
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Map<String, String>> end(@PathVariable String sessionId) {
        collabService.endSession(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "SESSION_ENDED"));
        return ResponseEntity.ok(Map.of("message", "Session ended"));
    }

    // POST /api/v1/sessions/{sessionId}/kick/{participantId}
    @PostMapping("/{sessionId}/kick/{participantId}")
    public ResponseEntity<Map<String, String>> kick(@PathVariable String sessionId,
                                                     @PathVariable String participantId) {
        collabService.kickParticipant(sessionId, participantId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "PARTICIPANT_KICKED", "participantId", participantId));
        return ResponseEntity.ok(Map.of("message", "Participant kicked"));
    }

    // GET /api/v1/sessions/{sessionId}/participants
    @GetMapping("/{sessionId}/participants")
    public ResponseEntity<List<Participant>> getParticipants(@PathVariable String sessionId) {
        return ResponseEntity.ok(collabService.getParticipants(sessionId));
    }

    // GET /api/v1/sessions/active
    @GetMapping("/active")
    public ResponseEntity<List<CollabSession>> getActive() {
        return ResponseEntity.ok(collabService.getActiveSessionsAll());
    }

    // ── WebSocket Handlers ────────────────────────────────────────────────

    // Client sends: /app/session/{sessionId}/cursor
    // Broadcast to: /topic/session/{sessionId}/cursors
    @MessageMapping("/session/{sessionId}/cursor")
    public void handleCursorUpdate(@Payload CursorMessage msg) {
        collabService.updateCursor(msg.getSessionId(), msg.getUserId(), msg.getLine(), msg.getCol());
        messagingTemplate.convertAndSend(
                "/topic/session/" + msg.getSessionId() + "/cursors", msg);
    }

    // Client sends: /app/session/{sessionId}/edit
    // Broadcast to: /topic/session/{sessionId}/edits
    @MessageMapping("/session/{sessionId}/edit")
    public void handleEdit(@Payload EditMessage msg) {
        // OT/CRDT would transform operations here in production
        messagingTemplate.convertAndSend(
                "/topic/session/" + msg.getSessionId() + "/edits", msg);
    }

    // ── Message DTOs ─────────────────────────────────────────────────────
    @Data
    public static class CursorMessage {
        private String sessionId;
        private String userId;
        private String username;
        private String color;
        private int line;
        private int col;
    }

    @Data
    public static class EditMessage {
        private String sessionId;
        private String userId;
        private String fileId;
        private String content;     // Full content (simple sync)
        private long timestamp;
    }
}
