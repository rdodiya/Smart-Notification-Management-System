package com.example.notification.service;

import com.example.notification.dto.DashboardResponseDTO;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements  DashboardService {

    private final NotificationRepository notificationRepository;

    @Override
    public DashboardResponseDTO getDashboardStatistics() {
        log.info("Fetching dashboard statistics");

        long totalNotifications = notificationRepository.count();
        long sentCount = notificationRepository.countByStatus(NotificationStatus.SENT);
        long failedCount = notificationRepository.countByStatus(NotificationStatus.FAILED);
        long pendingCount = notificationRepository.countByStatus(NotificationStatus.PENDING);
        long retryCount = notificationRepository.countRetried();

        Map<String, Long> typeWiseStats = getTypeWiseStatistics();

        return DashboardResponseDTO.builder()
                .totalNotifications(totalNotifications)
                .sentCount(sentCount)
                .failedCount(failedCount)
                .pendingCount(pendingCount)
                .retryCount(retryCount)
                .typeWiseStatistics(typeWiseStats)
                .build();
    }

    private Map<String, Long> getTypeWiseStatistics() {
        Map<String, Long> stats = new HashMap<>();
        Object[][] results = notificationRepository.countByType();

        for (Object[] result : results) {
            String type = result[0].toString();
            Long count = ((Number) result[1]).longValue();
            stats.put(type, count);
        }

        return stats;
    }
}