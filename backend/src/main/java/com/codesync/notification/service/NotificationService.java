package com.codesync.notification.service;

import com.codesync.notification.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification send(Notification notification);
    void sendBulk(List<String> recipientIds, Notification.NotificationType type,
                  String title, String message, String actorId);
    void markAsRead(String notificationId);
    void markAllRead(String recipientId);
    void deleteRead(String recipientId);
    List<Notification> getByRecipient(String recipientId);
    long getUnreadCount(String recipientId);
    void deleteNotification(String notificationId);
    List<Notification> getAll();
}
