package com.example.myspringproject.controller;

import com.example.myspringproject.dto.PaymentRequestDTO;
import com.example.myspringproject.dto.PaymentStatusResponseDTO;
import com.example.myspringproject.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/acct/payments")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'USER')")
    public ResponseEntity<PaymentStatusResponseDTO> payment(@RequestBody @Validated List<PaymentRequestDTO> paymentRequestDTO) {
        return ResponseEntity.ok(paymentService.loadPayments(paymentRequestDTO));
    }

    @PutMapping("/acct/payments")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'USER')")
    public ResponseEntity<PaymentStatusResponseDTO> updatePaymentsForUser(@RequestBody @Validated PaymentRequestDTO paymentRequestDTO) {
        return ResponseEntity.ok(paymentService.updatePayments(paymentRequestDTO));
    }
}
