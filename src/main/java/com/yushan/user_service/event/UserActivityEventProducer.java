package com.yushan.user_service.event;

import com.yushan.user_service.event.dto.UserActivityEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserActivityEventProducer {

    private static final String TOPIC = "active";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserActivityEvent(UserActivityEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.userId().toString(), event);
            log.info("Published user activity event for user: {}, service: {}, endpoint: {}",
                    event.userId(), event.serviceName(), event.endpoint());
        } catch (Exception e) {
            log.error("Failed to publish user activity event for user: {}", event.userId(), e);
        }
    }
}