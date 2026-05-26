package com.example.myspringproject.dto;

import com.example.myspringproject.entity.Operation;

public record UserAccessRequest(
        String user,
        Operation operation
) {
}
