package com.example.myspringproject.dto;

import com.example.myspringproject.entity.Operation;

public record UserAdminAccessRequestUserDTO(
        String user,
        Operation operation
) {
}
