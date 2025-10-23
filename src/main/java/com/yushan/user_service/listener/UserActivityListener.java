package com.yushan.user_service.listener;

import com.yushan.user_service.event.dto.UserActivityEvent;
import com.yushan.user_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserActivityListener {

    @Autowired
    private UserService userService;

    @KafkaListener(topics = "active", groupId = "user-service")
    public void handleUserActivity(UserActivityEvent event) {
        log.info("Received user activity event for userId: {}", event.userId());
        if (event.userId() != null) {
            userService.updateLastActiveTime(event.userId(), event.timestamp());
        }
    }
}

