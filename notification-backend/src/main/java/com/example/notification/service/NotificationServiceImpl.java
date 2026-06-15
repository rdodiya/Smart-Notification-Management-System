package com.example.notification.service;

import com.example.notification.dto.NotificationRequestDTO;
import com.example.notification.dto.NotificationResponseDTO;
import com.example.notification.dto.PageResponseDTO;
import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import com.example.notification.exception.DuplicateNotificationException;
import com.example.notification.exception.ResourceNotFoundException;
import com.example.notification.exception.RetryNotAllowedException;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.validator.MessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl  implements  NotificationService{

    private final NotificationRepository notificationRepository;
    private final MessageValidator messageValidator;
    private final NotificationProcessorService processorService;

    @Value("${notification.duplicate.check-interval-minutes:5}")
    private int duplicateCheckIntervalMinutes;

    @Value("${notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${notification.retry.min-interval-minutes:2}")
    private int retryMinIntervalMinutes;

    @Override
    @Transactional
    public NotificationResponseDTO createNotification(NotificationRequestDTO request) {
        log.info("Creating notification for user: {}, type: {}", request.getUserId(), request.getType());

        // Validate message
        messageValidator.validate(request.getMessage());

        // Check for duplicates
        checkDuplicateNotification(request);

        // Create notification entity
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .message(request.getMessage())
                .scheduleTime(request.getScheduleTime())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created with ID: {}", notification.getId());

        // Send to queue for processing
        processorService.processNotification(notification.getId());

        return mapToDTO(notification);
    }

    @Override
    public PageResponseDTO<NotificationResponseDTO> getNotifications(
            NotificationStatus status,
            NotificationType type,
            int page,
            int size) {

        log.info("Fetching notifications - status: {}, type: {}, page: {}, size: {}",
                status, type, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage;

        if (status != null && type != null) {
            notificationPage = notificationRepository.findByStatusAndType(status, type, pageable);
        } else if (status != null) {
            notificationPage = notificationRepository.findByStatus(status, pageable);
        } else if (type != null) {
            notificationPage = notificationRepository.findByType(type, pageable);
        } else {
            notificationPage = notificationRepository.findAll(pageable);
        }

        List<NotificationResponseDTO> content = notificationPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<NotificationResponseDTO>builder()
                .content(content)
                .pageNumber(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .last(notificationPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public NotificationResponseDTO retryNotification(Long id) {
        log.info("Retrying notification with ID: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        // Validate retry conditions
        validateRetryConditions(notification);

        // Update notification status
        notification.setStatus(NotificationStatus.RETRYING);
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setLastRetryTime(LocalDateTime.now());

        notification = notificationRepository.save(notification);
        log.info("Notification {} status updated to RETRYING, retry count: {}",
                id, notification.getRetryCount());

        // Send to queue for processing
        processorService.processNotification(notification.getId());

        return mapToDTO(notification);
    }

    private void checkDuplicateNotification(NotificationRequestDTO request) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(duplicateCheckIntervalMinutes);

        notificationRepository.findDuplicateNotification(
                request.getUserId(),
                request.getType(),
                request.getMessage(),
                since
        ).ifPresent(n -> {
            throw new DuplicateNotificationException(
                    String.format("Duplicate notification found. Same notification was created at %s",
                            n.getCreatedAt())
            );
        });
    }

    private void validateRetryConditions(Notification notification) {
        // Check if status is FAILED
        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new RetryNotAllowedException(
                    "Only FAILED notifications can be retried. Current status: " + notification.getStatus()
            );
        }

        // Check retry count
        if (notification.getRetryCount() >= maxRetryAttempts) {
            throw new RetryNotAllowedException(
                    String.format("Maximum retry attempts (%d) reached", maxRetryAttempts)
            );
        }

        // Check time since last retry
        if (notification.getLastRetryTime() != null) {
            LocalDateTime minRetryTime = notification.getLastRetryTime()
                    .plusMinutes(retryMinIntervalMinutes);

            if (LocalDateTime.now().isBefore(minRetryTime)) {
                throw new RetryNotAllowedException(
                        String.format("Please wait %d minutes before retrying", retryMinIntervalMinutes)
                );
            }
        }
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .retryCount(notification.getRetryCount())
                .scheduleTime(notification.getScheduleTime())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .errorMessage(notification.getErrorMessage())
                .build();
    }
}

