package com.yushan.user_service.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class UserActivityListener {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "active", groupId = "user-service")
    public void handleUserActivity(@Payload String payload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            JsonNode userIdNode = jsonNode.get("userId");
            JsonNode timestampNode = jsonNode.get("timestamp");
            if (userIdNode != null && !userIdNode.isNull() && timestampNode != null && !timestampNode.isNull()) {
                UUID uuid = UUID.fromString(userIdNode.asText());
                LocalDateTime timestamp = objectMapper.treeToValue(timestampNode, LocalDateTime.class);

                userService.updateLastActiveTime(uuid, timestamp);
                log.info("Successfully handled last active event for user: {}", uuid);
            } else {
                log.warn("Received user activity event with missing or null fields. Payload: {}", payload);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize UserActivityEvent", e);
        }
    }
}

