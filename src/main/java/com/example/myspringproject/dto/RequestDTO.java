package com.example.myspringproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestDTO(
        @NotNull(message = "Name must be not null")
        @NotBlank
        String name,

        @NotNull(message = "Last name must be not null")
        @NotBlank
        String lastName,

        @NotNull
        @NotBlank
        String email,

        @NotNull
        @NotBlank
        String password) {
}
