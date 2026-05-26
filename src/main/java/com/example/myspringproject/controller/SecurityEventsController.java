package com.example.myspringproject.controller;

import com.example.myspringproject.dto.SecurityEventResponse;
import com.example.myspringproject.service.SecurityEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SecurityEventsController {
    private final SecurityEventService securityEventService;

    @Autowired
    public SecurityEventsController(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @GetMapping("/security/events")
    @PreAuthorize("hasRole('AUDITOR')")
    public ResponseEntity<List<SecurityEventResponse>> getAllEvents() {
        return ResponseEntity.ok(securityEventService.getAllEvents());
    }
}
