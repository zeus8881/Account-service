package com.example.myspringproject.dto;

import jakarta.validation.constraints.NotNull;

public record ChangePasswordResponse(
        @NotNull
        String email,
        String message
) {
}
