package com.example.myspringproject.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String lastName,
        String email,
        List<String> roles
) {
}
