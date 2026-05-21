package com.example.myspringproject.dto;

import java.time.LocalDateTime;

public record SecurityEventsResponseDTO(
        LocalDateTime date,
        String action,
        String subject,
        String object,
        String path
) {
}
