package com.codesync.project.client;

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
     * Convenience: notify project owner about a fork.
     */
    public void notifyFork(String ownerId, String forkerUserId,
                            String projectId, String projectName) {
        sendNotification(
                ownerId, forkerUserId, "FORK",
                "Project forked: " + projectName,
                "Someone forked your project \"" + projectName + "\"",
                projectId, "PROJECT"
        );
    }

    /**
     * Convenience: notify about a new member added to a project.
     */
    public void notifyMemberAdded(String newMemberId, String addedByUserId,
                                    String projectId, String projectName) {
        sendNotification(
                newMemberId, addedByUserId, "BROADCAST",
                "Added to project: " + projectName,
                "You have been added as a member to \"" + projectName + "\"",
                projectId, "PROJECT"
        );
    }

    /**
     * Convenience: notify project owner about a star.
     */
    public void notifyStar(String ownerId, String starrerId,
                            String projectId, String projectName) {
        sendNotification(
                ownerId, starrerId, "BROADCAST",
                "Project starred: " + projectName,
                "Someone starred your project \"" + projectName + "\"",
                projectId, "PROJECT"
        );
    }
}
