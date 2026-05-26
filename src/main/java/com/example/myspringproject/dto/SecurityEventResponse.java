package com.example.myspringproject.dto;

import java.time.LocalDateTime;

public record SecurityEventResponse(
        LocalDateTime date,
        String action,
        String subject,
        String object,
        String path
) {
}
