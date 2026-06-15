package com.example.notification.controller;

import com.example.notification.dto.NotificationRequestDTO;
import com.example.notification.dto.NotificationResponseDTO;
import com.example.notification.dto.PageResponseDTO;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationRequestDTO request) {
        log.info("Received create notification request for user: {}", request.getUserId());
        NotificationResponseDTO response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<NotificationResponseDTO>> getNotifications(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received get notifications request - status: {}, type: {}, page: {}, size: {}",
                status, type, page, size);

        PageResponseDTO<NotificationResponseDTO> response =
                notificationService.getNotifications(status, type, page, size);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationResponseDTO> retryNotification(@PathVariable Long id) {
        log.info("Received retry request for notification ID: {}", id);
        NotificationResponseDTO response = notificationService.retryNotification(id);
        return ResponseEntity.ok(response);
    }
}