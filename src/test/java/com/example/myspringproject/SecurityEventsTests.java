package com.example.myspringproject;

import com.example.myspringproject.dto.SecurityEventResponse;
import com.example.myspringproject.entity.ActionName;
import com.example.myspringproject.service.SecurityEventService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityEventsTests {

    static {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private SecurityEventService securityEventService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getAllSecurityEventsTest() throws Exception {
        SecurityEventResponse response1 = new SecurityEventResponse(LocalDateTime.now(), "CREATE_USER", "Anonymous", "user@acme.com", "api/auth/signup");
        SecurityEventResponse response2 = new SecurityEventResponse(LocalDateTime.now(), "CHANGE_PASSWORD", "Anonymous", "user@acme.com", "api/auth/signup");

        List<SecurityEventResponse> securityEventResponses = List.of(response1, response2);

        Mockito.when(securityEventService.getAllEvents()).thenReturn((securityEventResponses));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/security/events")
                .with(user("auditor@acme.com").roles("AUDITOR"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].action").value("CREATE_USER"))
                .andExpect(jsonPath("$.[1].action").value("CHANGE_PASSWORD"));
    }
}
