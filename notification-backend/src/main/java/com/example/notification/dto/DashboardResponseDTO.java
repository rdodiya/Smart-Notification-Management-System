package com.example.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private Long totalNotifications;
    private Long sentCount;
    private Long failedCount;
    private Long retryCount;
    private Long pendingCount;
    private Map<String, Long> typeWiseStatistics;
}
