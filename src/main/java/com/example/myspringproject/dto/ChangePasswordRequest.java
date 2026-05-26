package com.example.myspringproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotNull
        @Size(min = 12, message = "Password length must be 12 chars minimum!")
        @JsonProperty("new_password")
        String newPassword
) {
}
