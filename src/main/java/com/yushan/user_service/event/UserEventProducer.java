package com.yushan.user_service.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.event.dto.EventEnvelope;
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
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        send(event.getClass().getSimpleName(), event);
    }

    public void sendUserLoggedInEvent(UserLoggedInEvent event) {
        send(event.getClass().getSimpleName(), event);
    }

    private void send(String eventType, Object payload) {
        try {
            JsonNode payloadJson = objectMapper.valueToTree(payload);
            EventEnvelope envelope = new EventEnvelope(eventType, payloadJson);

            log.info("Sending event in envelope [type={}] to topic {}", eventType, TOPIC);
            kafkaTemplate.send(TOPIC, envelope);
        } catch (Exception e) {
            log.error("Error sending event envelope for type {}", eventType, e);
        }
    }
}