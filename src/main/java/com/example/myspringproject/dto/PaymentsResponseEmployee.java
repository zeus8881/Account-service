package com.example.myspringproject.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentsResponseEmployee(
        @NotNull
        String name,

        @NotNull
        String lastName,

        @NotNull
        String period,

        String salary
) {
}
