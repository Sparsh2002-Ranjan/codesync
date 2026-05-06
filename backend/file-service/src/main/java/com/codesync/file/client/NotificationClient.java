package com.codesync.file.client;

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
     * Convenience: notify about file creation in a project.
     */
    public void notifyFileCreated(String recipientId, String actorId,
                                   String fileId, String fileName) {
        sendNotification(
                recipientId, actorId, "BROADCAST",
                "New file: " + fileName,
                "A new file \"" + fileName + "\" was created in your project",
                fileId, "FILE"
        );
    }

    /**
     * Convenience: notify about file deletion in a project.
     */
    public void notifyFileDeleted(String recipientId, String actorId,
                                   String fileId, String fileName) {
        sendNotification(
                recipientId, actorId, "BROADCAST",
                "File deleted: " + fileName,
                "The file \"" + fileName + "\" was deleted from your project",
                fileId, "FILE"
        );
    }
}
