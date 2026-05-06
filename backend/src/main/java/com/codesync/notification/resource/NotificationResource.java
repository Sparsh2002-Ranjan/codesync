package com.codesync.notification.resource;

import com.codesync.notification.entity.Notification;
import com.codesync.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationResource {

    private final NotificationService notificationService;

    // POST /api/v1/notifications
    @PostMapping
    public ResponseEntity<Notification> send(@RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.send(notification));
    }

    // POST /api/v1/notifications/bulk
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, String>> sendBulk(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> recipientIds = (List<String>) body.get("recipientIds");
        notificationService.sendBulk(
                recipientIds,
                Notification.NotificationType.valueOf((String) body.get("type")),
                (String) body.get("title"),
                (String) body.get("message"),
                (String) body.get("actorId")
        );
        return ResponseEntity.ok(Map.of("message", "Bulk notifications sent"));
    }

    // GET /api/v1/notifications/recipient/{recipientId}
    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<Notification>> getByRecipient(@PathVariable String recipientId) {
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId));
    }

    // GET /api/v1/notifications/recipient/{recipientId}/unread-count
    @GetMapping("/recipient/{recipientId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String recipientId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(recipientId)));
    }

    // PUT /api/v1/notifications/{notificationId}/read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    // PUT /api/v1/notifications/recipient/{recipientId}/read-all
    @PutMapping("/recipient/{recipientId}/read-all")
    public ResponseEntity<Map<String, String>> markAllRead(@PathVariable String recipientId) {
        notificationService.markAllRead(recipientId);
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    // DELETE /api/v1/notifications/recipient/{recipientId}/read
    @DeleteMapping("/recipient/{recipientId}/read")
    public ResponseEntity<Map<String, String>> deleteRead(@PathVariable String recipientId) {
        notificationService.deleteRead(recipientId);
        return ResponseEntity.ok(Map.of("message", "Read notifications deleted"));
    }

    // DELETE /api/v1/notifications/{notificationId}
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    // GET /api/v1/notifications/all  (Admin)
    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }
}
