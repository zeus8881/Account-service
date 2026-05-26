package com.example.myspringproject.dto;

public record SignupResponseDTO(
        Long id,
        String name,
        String lastName,
        String email) {
}
