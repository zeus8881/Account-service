package com.example.myspringproject;

import com.example.myspringproject.dto.SignupRequestDTO;
import com.example.myspringproject.dto.SignupResponseDTO;
import com.example.myspringproject.dto.UserResponse;
import com.example.myspringproject.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.List;
import java.util.TimeZone;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MySpringProjectApplicationTests {


    static {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void signupWithValidData() throws Exception {
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO("Ivan", "Ivanov", "ivanov@acme.com", "password12345");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void apiPaymentForUserRole() throws Exception {
        SignupResponseDTO mockResponse = new SignupResponseDTO(778L, "Test Ivan", "Ivanov", "ivanov@acme.com");

        Mockito.when(userService.getApiPayment(Mockito.any(Principal.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/empl/payments")
                        .with(user("ivanov@acme.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(778));
    }

    @Test
    void apiPaymentAdminForForbiddenRole() throws Exception {
        SignupResponseDTO mockResponse = new SignupResponseDTO(778L, "Test Ivan", "Ivanov", "ivanov@acme.com");

        Mockito.when(userService.getApiPayment(Mockito.any(Principal.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/empl/payments")
                        .with(user("ivanov@acme.com").roles("ADMINISTRATOR"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@acme.com", roles = "ADMINISTRATOR")
    void getAllUsers() throws Exception {
        UserResponse user1 = new UserResponse(1L, "Ivan", "Ivanov", "ivan@acme.com", List.of());
        UserResponse user2 = new UserResponse(2L, "Petro", "Petrov", "petrov@acme.com", List.of());

        List<UserResponse> users = List.of(user1, user2);
        Mockito.when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/admin/user")
                .with(user("admin@acme.com").roles("ADMINISTRATOR"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("ivan@acme.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].email").value("petrov@acme.com"));
    }

    @Test
    void getAllUsersWhenUserIsNot() throws Exception {
        mockMvc.perform(get("/api/admin/user")
                .with(user("user@acme.com").roles("USER"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
