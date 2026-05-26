package com.example.myspringproject.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull
        String employee,

        @NotNull
        String period,

        Long salary
) {
}
