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
public class CollabResource {

    private final CollabService collabService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<CollabSession> createSession(@RequestBody CollabSession session) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collabService.createSession(session));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<CollabSession> getById(@PathVariable String sessionId) {
        return ResponseEntity.ok(collabService.getSessionById(sessionId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CollabSession>> getByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(collabService.getSessionsByProject(projectId));
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<Participant> join(@PathVariable String sessionId,
                                             @RequestParam String userId,
                                             @RequestParam(required = false) String password) {
        Participant p = collabService.joinSession(sessionId, userId, password);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "PARTICIPANT_JOINED", "userId", userId));
        return ResponseEntity.ok(p);
    }

    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<Map<String, String>> leave(@PathVariable String sessionId,
                                                      @RequestParam String userId) {
        collabService.leaveSession(sessionId, userId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "PARTICIPANT_LEFT", "userId", userId));
        return ResponseEntity.ok(Map.of("message", "Left session"));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Map<String, String>> end(@PathVariable String sessionId) {
        collabService.endSession(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "SESSION_ENDED"));
        return ResponseEntity.ok(Map.of("message", "Session ended"));
    }

    @PostMapping("/{sessionId}/kick/{participantId}")
    public ResponseEntity<Map<String, String>> kick(@PathVariable String sessionId,
                                                     @PathVariable String participantId) {
        collabService.kickParticipant(sessionId, participantId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "PARTICIPANT_KICKED", "participantId", participantId));
        return ResponseEntity.ok(Map.of("message", "Participant kicked"));
    }

    @GetMapping("/{sessionId}/participants")
    public ResponseEntity<List<Participant>> getParticipants(@PathVariable String sessionId) {
        return ResponseEntity.ok(collabService.getParticipants(sessionId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CollabSession>> getActive() {
        return ResponseEntity.ok(collabService.getActiveSessionsAll());
    }

    /**
     * Called when a user arrives via a direct share link.
     * Notifies the session HOST with a JOIN_REQUEST notification (via WebSocket)
     * so they can Accept or Reject.
     */
    @PostMapping("/{sessionId}/request-join")
    public ResponseEntity<Map<String, String>> requestJoin(
            @PathVariable String sessionId,
            @RequestParam String userId,
            @RequestParam String username) {
        CollabSession session = collabService.getSessionById(sessionId);
        // Send real-time notification to the host on their personal queue
        messagingTemplate.convertAndSendToUser(
                session.getOwnerId(),
                "/queue/notifications",
                Map.of(
                    "type", "JOIN_REQUEST",
                    "sessionId", sessionId,
                    "requestingUserId", userId,
                    "requestingUsername", username,
                    "projectId", session.getProjectId()
                )
        );
        return ResponseEntity.ok(Map.of("message", "Join request sent to host"));
    }

    /**
     * Host calls this to accept a join request.
     * Adds the user as a participant and notifies them to redirect into the session.
     */
    @PostMapping("/{sessionId}/accept-join")
    public ResponseEntity<Participant> acceptJoin(
            @PathVariable String sessionId,
            @RequestParam String userId) {
        Participant p = collabService.joinSession(sessionId, userId, null);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events",
                Map.of("type", "PARTICIPANT_JOINED", "userId", userId));
        // Notify the accepted user on their personal queue
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                Map.of(
                    "type", "JOIN_ACCEPTED",
                    "sessionId", sessionId,
                    "projectId", collabService.getSessionById(sessionId).getProjectId()
                )
        );
        return ResponseEntity.ok(p);
    }

    // ── WebSocket Handlers ─────────────────────────────────────────────────
    // NOTE: STOMP @MessageMapping does NOT support path variables like {sessionId}.
    // The sessionId is carried inside the payload and routed there.

    @MessageMapping("/session.cursor")
    public void handleCursorUpdate(@Payload CursorMessage msg) {
        collabService.updateCursor(msg.getSessionId(), msg.getUserId(), msg.getLine(), msg.getCol());
        messagingTemplate.convertAndSend("/topic/session/" + msg.getSessionId() + "/cursors", msg);
    }

    @MessageMapping("/session.edit")
    public void handleEdit(@Payload EditMessage msg) {
        messagingTemplate.convertAndSend("/topic/session/" + msg.getSessionId() + "/edits", msg);
    }

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
        private String content;
        private long timestamp;
    }
}
