package com.codesync.comment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
     * Send a notification to a specific user via the Notification Service REST API.
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
            // Fire-and-forget: log but do not propagate
            log.warn("Failed to send notification to {}: {}", recipientId, e.getMessage());
        }
    }

    /**
     * Convenience: send a COMMENT notification.
     */
    public void notifyComment(String recipientId, String actorId,
                               String commentId, String fileName, int lineNumber) {
        sendNotification(
                recipientId, actorId, "COMMENT",
                "New comment on " + fileName,
                "A comment was added on line " + lineNumber + " of " + fileName,
                commentId, "COMMENT"
        );
    }

    /**
     * Convenience: send a MENTION notification.
     */
    public void notifyMention(String recipientId, String actorId,
                               String commentId, String fileName) {
        sendNotification(
                recipientId, actorId, "MENTION",
                "You were mentioned in " + fileName,
                "Someone mentioned you in a comment",
                commentId, "COMMENT"
        );
    }
}
