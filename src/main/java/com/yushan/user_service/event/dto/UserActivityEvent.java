package com.yushan.user_service.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserActivityEvent(
        UUID userId,
        String serviceName,
        String endpoint,
        String method,
        LocalDateTime timestamp
) {}
