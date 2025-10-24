package com.yushan.user_service.event;

import com.yushan.user_service.event.dto.UserActivityEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserActivityEventProducer userActivityEventProducer;

    @Test
    void sendUserActivityEvent_shouldSendEventToKafka() {
        // Given
        UUID userId = UUID.randomUUID();
        UserActivityEvent event = new UserActivityEvent(userId, "test-service", "/test", "", LocalDateTime.now());
        String expectedTopic = "active";
        String expectedKey = userId.toString();

        // When
        userActivityEventProducer.sendUserActivityEvent(event);

        // Then
        verify(kafkaTemplate, times(1)).send(expectedTopic, expectedKey, event);
    }

    @Test
    void sendUserActivityEvent_shouldHandleExceptionAndLog() {
        // Given
        UUID userId = UUID.randomUUID();
        UserActivityEvent event = new UserActivityEvent(userId, "test-service", "/test", "", LocalDateTime.now());
        String expectedTopic = "active";
        String expectedKey = userId.toString();

        // Simulate an exception when sending to Kafka
        doThrow(new RuntimeException("Kafka connection failed")).when(kafkaTemplate).send(expectedTopic, expectedKey, event);

        // When
        userActivityEventProducer.sendUserActivityEvent(event);

        // Then
        // Verify that the send method was still called
        verify(kafkaTemplate, times(1)).send(expectedTopic, expectedKey, event);
        // The test passes if no exception is thrown, as it should be caught and logged.
    }
}
