package com.example.myspringproject;

import com.example.myspringproject.dto.PaymentRequest;
import com.example.myspringproject.dto.PaymentResponse;
import com.example.myspringproject.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.TimeZone;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PaymentTests {

    static {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @MockitoBean
    PaymentService paymentService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void LoadPaymentTest() throws Exception {
        PaymentRequest paymentRequest1 = new PaymentRequest("ivanov@acme.com", "01-2026", 1000L);
        PaymentRequest paymentRequest2 = new PaymentRequest("ivanov@acme.com", "02-2026", 5000L);
        PaymentResponse paymentResponse = new PaymentResponse("Added successfully!");

        List<PaymentRequest> payments = List.of(paymentRequest1, paymentRequest2);

        Mockito.when(paymentService.loadPayments(Mockito.anyList())).thenReturn(paymentResponse);

        mockMvc.perform(post("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payments))
                        .with(user("accountant@acme.com").roles("ACCOUNTANT"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Added successfully!"));
    }

    @Test
    void updatePaymentTest() throws Exception {
        PaymentRequest paymentRequest = new PaymentRequest("ivan@acme.com", "01-2026", 1000L);
        PaymentResponse paymentResponse = new PaymentResponse("Updated successfully!");

        Mockito.when(paymentService.updatePayments(Mockito.any(PaymentRequest.class)))
                .thenReturn(paymentResponse);

        mockMvc.perform(put("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest))
                        .with(user("accountant@acme.com").roles("ACCOUNTANT"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Updated successfully!"));
    }

    @Test
    void updatePaymentsTestWhenSalaryIsNegative() throws Exception {
        PaymentRequest paymentRequest = new PaymentRequest("ivan@acme.com", "01-2026", -100L);

        Mockito.when(paymentService.updatePayments(Mockito.any(PaymentRequest.class)))
                .thenThrow(new IllegalArgumentException("Salary must be greater than 0!"));

        mockMvc.perform(put("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest))
                        .with(user("accountant@acme.com").roles("ACCOUNTANT"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
