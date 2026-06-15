package com.example.notification.dto;

import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String message;
    private NotificationStatus status;
    private Integer retryCount;
    private LocalDateTime scheduleTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String errorMessage;
}

