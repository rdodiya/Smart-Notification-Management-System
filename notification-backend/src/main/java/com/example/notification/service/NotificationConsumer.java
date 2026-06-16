package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = "${notification.queue.name}")
    @Transactional
    public void consumeNotification(Long notificationId) {
        log.info("Received notification {} from queue", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        try {
            // Simulate sending logic
            log.info("Sending notification to user {}", notification.getUserId());

            // ✅ Add real Email/SMS logic here
            if(notification.getType().equals(NotificationType.EMAIL)){
                
            }
            notification.setStatus(NotificationStatus.SENT);
            notification.setErrorMessage(null);

        } catch (Exception ex) {

            log.error("Failed to send notification {}", notificationId, ex);

            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(ex.getMessage());
        }

        notificationRepository.save(notification);
    }
}