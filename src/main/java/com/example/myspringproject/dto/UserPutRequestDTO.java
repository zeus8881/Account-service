package com.example.myspringproject.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserPutRequestDTO(
        @NotNull
        String user,
        String role,
        String operation
) {
}
