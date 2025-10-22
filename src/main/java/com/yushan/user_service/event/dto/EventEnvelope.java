package com.yushan.user_service.event.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record EventEnvelope(
        String eventType,
        JsonNode payload
) {}