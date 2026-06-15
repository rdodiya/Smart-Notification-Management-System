package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessorService {

    private final NotificationRepository notificationRepository;
    private final Random random = new Random();

    @Value("${notification.failure.rate:0.3}")
    private double failureRate;

    @Async
    @Transactional
    public void processNotification(Long notificationId) {
        log.info("Processing notification ID: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        try {
            // Simulate processing delay
            Thread.sleep(1000 + random.nextInt(2000));

            // Simulate random failure (30% chance)
            boolean shouldFail = random.nextDouble() < failureRate;

            if (shouldFail) {
                handleFailure(notification);
            } else {
                handleSuccess(notification);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing interrupted for notification {}", notificationId, e);
            handleFailure(notification);
        } catch (Exception e) {
            log.error("Error processing notification {}", notificationId, e);
            handleFailure(notification);
        }
    }

    private void handleSuccess(Notification notification) {
        notification.setStatus(NotificationStatus.SENT);
        notification.setErrorMessage(null);
        notificationRepository.save(notification);

        log.info("Notification {} sent successfully via {}",
                notification.getId(), notification.getType());
    }

    private void handleFailure(Notification notification) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage("Simulated failure - Service temporarily unavailable");
        notificationRepository.save(notification);

        log.warn("Notification {} failed. Retry count: {}",
                notification.getId(), notification.getRetryCount());
    }
}