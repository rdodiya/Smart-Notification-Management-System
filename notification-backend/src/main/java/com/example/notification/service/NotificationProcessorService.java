package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.exchange.name}")
    private String exchangeName;

    @Value("${notification.routing.key}")
    private String routingKey;


    @Async
    @Transactional
    public void processNotification(Long notificationId) {
        log.info("Processing notification ID: {}", notificationId);
        log.info("Sending notification {} to RabbitMQ", notificationId);

        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                notificationId
        );
    }
}