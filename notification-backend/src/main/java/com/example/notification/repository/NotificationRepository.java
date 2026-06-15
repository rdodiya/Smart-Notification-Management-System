package com.example.notification.repository;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByStatusAndType(NotificationStatus status, NotificationType type, Pageable pageable);

    long countByStatus(NotificationStatus status);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.retryCount > 0")
    long countRetried();

    @Query("SELECT n.type as type, COUNT(n) as count FROM Notification n GROUP BY n.type")
    Object[][] countByType();

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.type = :type " +
            "AND n.message = :message " +
            "AND n.createdAt > :since")
    Optional<Notification> findDuplicateNotification(
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            @Param("message") String message,
            @Param("since") LocalDateTime since
    );
}
