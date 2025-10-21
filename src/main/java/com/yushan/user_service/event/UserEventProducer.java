package com.yushan.user_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.event.dto.UserLoggedInEvent;
import com.yushan.user_service.event.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

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
            log.info("Attempting to send UserLoggedInEvent to topic {}: {}", TOPIC, message);

            // add callback using CompletableFuture to confirm send result
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent UserLoggedInEvent, offset: {}, partition: {}",
                            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send UserLoggedInEvent", ex);
                }
            });
        } catch (Exception e) {
            log.error("Error before sending UserLoggedInEvent", e);
        }
    }

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            log.info("Attempting to send UserRegisteredEvent to topic {}: {}", TOPIC, message);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent UserRegisteredEvent, offset: {}, partition: {}",
                            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send UserRegisteredEvent", ex);
                }
            });
        } catch (Exception e) {
            log.error("Error before sending UserRegisteredEvent", e);
        }
    }
}
