package com.example.myspringproject.exception;

import com.example.myspringproject.service.SecurityEventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.example.myspringproject.entity.ActionName.ACCESS_DENIED;

@Component
public class AccessDeniedHandlerException implements AccessDeniedHandler {
    private final SecurityEventService securityEventService;

    public AccessDeniedHandlerException(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @Override
    public void handle(@NonNull HttpServletRequest request,
                       @NonNull HttpServletResponse response,
                       @NonNull AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String subject = authentication.getName();

        securityEventService.eventSecurity(
                LocalDateTime.now(),
                ACCESS_DENIED,
                subject,
                request.getRequestURI(),
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("""
                {
                "error": "Forbidden",
                "message": "Access Denied!"
                }
                """);
    }
}
