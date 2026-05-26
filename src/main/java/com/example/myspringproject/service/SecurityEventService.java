package com.example.myspringproject.service;

import com.example.myspringproject.dto.SecurityEventResponse;
import com.example.myspringproject.entity.ActionName;
import com.example.myspringproject.entity.SecurityEvent;
import com.example.myspringproject.repository.SecurityEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class SecurityEventService {
    private final SecurityEventRepository securityEventRepository;

    @Autowired
    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
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

    public List<SecurityEventResponse> getAllEvents() {
        List<SecurityEvent> securityEventLIst = securityEventRepository.findAll();

        return securityEventLIst.stream()
                .sorted(Comparator.comparing(SecurityEvent::getId))
                .map(securityEvent -> new SecurityEventResponse(
                        securityEvent.getDate(),
                        securityEvent.getAction().name(),
                        securityEvent.getSubject(),
                        securityEvent.getObject(),
                        securityEvent.getPath()
                ))
                .toList();
    }
}
