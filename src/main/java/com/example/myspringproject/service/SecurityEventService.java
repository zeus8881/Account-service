package com.example.myspringproject.service;

import com.example.myspringproject.dto.SecurityEventsResponseDTO;
import com.example.myspringproject.entity.ActionName;
import com.example.myspringproject.entity.SecurityEvent;
import com.example.myspringproject.entity.User;
import com.example.myspringproject.repository.SecurityEventRepository;
import com.example.myspringproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class SecurityEventService {
    private final SecurityEventRepository securityEventRepository;
    private final UserRepository userRepository;

    @Autowired
    public SecurityEventService(SecurityEventRepository securityEventRepository, UserRepository userRepository) {
        this.securityEventRepository = securityEventRepository;
        this.userRepository = userRepository;
    }

    public void eventSecurity(LocalDateTime date, ActionName action, String subject, String object, String path) {
        SecurityEvent securityEvent = new SecurityEvent();
        securityEvent.setDate(date);
        securityEvent.setAction(action);
        securityEvent.setSubject(subject);
        securityEvent.setObject(object);
        securityEvent.setPath(path);

        securityEventRepository.save(securityEvent);
    }

    public List<SecurityEventsResponseDTO> getAllEvents(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("User not found."));
        List<SecurityEvent> securityEventLIst = securityEventRepository.findAll();
        return List.of();
    }
}
