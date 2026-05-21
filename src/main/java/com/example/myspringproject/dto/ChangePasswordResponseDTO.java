package com.example.myspringproject.dto;

import jakarta.validation.constraints.NotNull;

public record ChangePasswordResponseDTO(
        @NotNull
        String email,
        String message
) {
}
