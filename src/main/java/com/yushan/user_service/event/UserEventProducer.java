package com.yushan.user_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.event.dto.UserLoggedInEvent;
import com.yushan.user_service.event.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserEventProducer {

    private static final String TOPIC = "user.events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendUserLoggedInEvent(UserLoggedInEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            log.info("Sending sendUserLoggedInEvent to topic {}: {}", TOPIC, message);
            kafkaTemplate.send(TOPIC, message);
        } catch (Exception e) {
            log.error("Failed to send UserLoggedInEvent", e);
        }
    }

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            log.info("Sending UserRegisteredEvent to topic {}: {}", TOPIC, message);
            kafkaTemplate.send(TOPIC, message);
        } catch (Exception e) {
            log.error("Failed to send UserRegisteredEvent", e);
        }
    }
}