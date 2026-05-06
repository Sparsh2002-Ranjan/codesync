package com.codesync.notification.service;

import com.codesync.notification.entity.Notification;
import com.codesync.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Notification send(Notification notification) {
        Notification saved = notificationRepository.save(notification);

        // Real-time push via WebSocket to recipient
        // Frontend subscribes to: /user/{userId}/queue/notifications
        long unreadCount = getUnreadCount(notification.getRecipientId());
        messagingTemplate.convertAndSendToUser(
                notification.getRecipientId(),
                "/queue/notifications",
                Map.of(
                    "notification", saved,
                    "unreadCount", unreadCount
                )
        );

        log.info("Notification sent to user {}: {}", notification.getRecipientId(), notification.getTitle());
        return saved;
    }

    @Override
    @Transactional
    public void sendBulk(List<String> recipientIds, Notification.NotificationType type,
                         String title, String message, String actorId) {
        recipientIds.forEach(recipientId -> {
            Notification notif = Notification.builder()
                    .recipientId(recipientId)
                    .actorId(actorId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .build();
            send(notif);
        });
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllRead(String recipientId) {
        List<Notification> unread = notificationRepository
                .findByRecipientIdAndIsRead(recipientId, false);
        unread.forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void deleteRead(String recipientId) {
        notificationRepository.deleteByRecipientIdAndIsRead(recipientId, true);
    }

    @Override
    public List<Notification> getByRecipient(String recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    @Override
    public long getUnreadCount(String recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }
}
