package com.yushan.user_service.event;

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
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserLoggedInEvent(UserLoggedInEvent event) {
        log.info("Attempting to send UserLoggedInEvent to topic {}", TOPIC);
        try {
            kafkaTemplate.send(TOPIC, event);
        } catch (Exception e) {
            log.error("Error sending UserLoggedInEvent", e);
        }
    }

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Attempting to send UserRegisteredEvent to topic {}", TOPIC);
        try {
            kafkaTemplate.send(TOPIC, event);
        } catch (Exception e) {
            log.error("Error sending UserRegisteredEvent", e);
        }
    }
}