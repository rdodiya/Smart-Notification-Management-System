package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @Value("${notification.failure.rate:0.5}")
    private double failureRate;

    private final Random random = new Random();

    @RabbitListener(queues = "${notification.queue.name}")
    @Transactional
    public void consumeNotification(Long notificationId) {
        log.info("Received notification {} from queue", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        try {
            // Simulate sending logic
            log.info("Sending notification to user {}", notification.getUserId());

            // Simulate random failure (30% chance)
            boolean shouldFail = random.nextDouble() < failureRate;
            if (shouldFail) {
                handleFailure(notification);
            } else {
                handleSuccess(notification);
            }
        } catch (Exception ex) {
            log.error("Failed to send notification {}", notificationId, ex);

            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(ex.getMessage());

            notificationRepository.save(notification);
        }
    }

    private void sendNotification(Notification notification) {
        switch (notification.getType()) {
            case SMS -> log.info("Sending SMS notification");
            case PUSH -> log.info("Sending PUSH notification");
            case EMAIL -> log.info("Sending EMAIL notification");
            default -> log.warn("Unsupported notification type: {}", notification.getType());
        }
    }

    private void handleSuccess(Notification notification) {
        sendNotification(notification);
        notification.setStatus(NotificationStatus.SENT);
        notification.setErrorMessage(null);
        notificationRepository.save(notification);

        log.info("Notification {} sent successfully via {}",
                notification.getId(), notification.getType());
    }

    private void handleFailure(Notification notification) {
        sendNotification(notification);
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage("Simulated failure - Service temporarily unavailable");
        notificationRepository.save(notification);

        log.warn("Notification {} failed. Retry count: {}",
                notification.getId(), notification.getRetryCount());
    }
}