package com.example.myspringproject.controller;

import com.example.myspringproject.dto.PaymentRequest;
import com.example.myspringproject.dto.PaymentResponse;
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
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<PaymentResponse> payment(@RequestBody @Validated List<PaymentRequest> paymentRequest) {
        return ResponseEntity.ok(paymentService.loadPayments(paymentRequest));
    }

    @PutMapping("/acct/payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<PaymentResponse> updatePaymentsForUser(@RequestBody @Validated PaymentRequest paymentRequest) {
        return ResponseEntity.ok(paymentService.updatePayments(paymentRequest));
    }
}
