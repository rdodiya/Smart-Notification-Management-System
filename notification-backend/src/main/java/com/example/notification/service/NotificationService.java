package com.example.notification.service;

import com.example.notification.dto.NotificationRequestDTO;
import com.example.notification.dto.NotificationResponseDTO;
import com.example.notification.dto.PageResponseDTO;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;

public interface NotificationService {
    public NotificationResponseDTO createNotification(NotificationRequestDTO request);
    public PageResponseDTO<NotificationResponseDTO> getNotifications(
            NotificationStatus status,
            NotificationType type,
            int page,
            int size);
    public NotificationResponseDTO retryNotification(Long id);
}
