package com.example.myspringproject.dto;

import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotNull
        String user,
        String role,
        String operation
) {
}
