package com.example.myspringproject.exception;

import com.example.myspringproject.entity.ActionName;
import com.example.myspringproject.entity.User;
import com.example.myspringproject.repository.UserRepository;
import com.example.myspringproject.service.SecurityEventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.example.myspringproject.entity.ActionName.*;

@Component
public class FailureHandler implements AuthenticationFailureHandler {
    private final UserRepository userRepository;
    private final SecurityEventService securityEventService;

    public FailureHandler(UserRepository userRepository, SecurityEventService securityEventService) {
        this.userRepository = userRepository;
        this.securityEventService = securityEventService;
    }


    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("email");
        User user = null;
        if (email != null) {
            user = userRepository.findByEmail(email)
                    .orElse(null);
        }
        String subject = (email != null) ? email : "Anonymous";
        securityEventService.eventSecurity(
                LocalDateTime.now(),
                LOGIN_FAILED,
                subject,
                request.getRequestURI(),
                request.getRequestURI()
        );
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= 5) {
            securityEventService.eventSecurity(
                    LocalDateTime.now(),
                    BRUTE_FORCE,
                    user.getEmail(),
                    request.getRequestURI(),
                    request.getRequestURI()
            );
            user.setLocked(true);
            userRepository.save(user);

            securityEventService.eventSecurity(
                    LocalDateTime.now(),
                    LOCK_USER,
                    user.getEmail(),
                    "Lock user: " + user.getEmail(),
                    request.getRequestURI()
            );
        }
    }
}
