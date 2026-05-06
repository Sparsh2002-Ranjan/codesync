package com.codesync.collab.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for sending notifications to the Notification Service.
 * Uses fire-and-forget pattern — failures are logged but never propagate.
 */
@Component
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification.service.url}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    /**
     * Send a notification to a specific user.
     */
    public void sendNotification(String recipientId, String actorId, String type,
                                  String title, String message,
                                  String relatedId, String relatedType) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("recipientId", recipientId);
            body.put("actorId", actorId);
            body.put("type", type);
            body.put("title", title);
            body.put("message", message);
            body.put("relatedId", relatedId);
            body.put("relatedType", relatedType);

            restTemplate.postForObject(
                    notificationServiceUrl + "/api/v1/notifications",
                    body,
                    Map.class
            );
            log.debug("Notification sent: {} → {} [{}]", actorId, recipientId, type);
        } catch (Exception e) {
            log.warn("Failed to send notification to {}: {}", recipientId, e.getMessage());
        }
    }

    /**
     * Send bulk notifications (e.g., to all participants in a session).
     */
    public void sendBulkNotification(List<String> recipientIds, String actorId, String type,
                                      String title, String message) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("recipientIds", recipientIds);
            body.put("actorId", actorId);
            body.put("type", type);
            body.put("title", title);
            body.put("message", message);

            restTemplate.postForObject(
                    notificationServiceUrl + "/api/v1/notifications/bulk",
                    body,
                    Map.class
            );
            log.debug("Bulk notification sent to {} recipients [{}]", recipientIds.size(), type);
        } catch (Exception e) {
            log.warn("Failed to send bulk notification: {}", e.getMessage());
        }
    }

    /**
     * Convenience: notify session participants that someone joined.
     */
    public void notifyParticipantJoined(List<String> existingParticipantIds,
                                         String joinedUserId, String sessionId) {
        sendBulkNotification(
                existingParticipantIds, joinedUserId, "PARTICIPANT_JOINED",
                "User joined session",
                "A collaborator has joined the editing session"
        );
    }

    /**
     * Convenience: notify when a session is created (invite style).
     */
    public void notifySessionCreated(String recipientId, String ownerId, String sessionId) {
        sendNotification(
                recipientId, ownerId, "SESSION_INVITE",
                "Collaboration session started",
                "You've been invited to a live collaboration session",
                sessionId, "SESSION"
        );
    }
}
