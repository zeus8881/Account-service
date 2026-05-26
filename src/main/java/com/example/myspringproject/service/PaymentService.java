package com.example.myspringproject.service;


import com.example.myspringproject.dto.PaymentRequest;
import com.example.myspringproject.dto.PaymentResponse;
import com.example.myspringproject.dto.PaymentsResponseEmployee;
import com.example.myspringproject.entity.Payment;
import com.example.myspringproject.entity.User;
import com.example.myspringproject.repository.PaymentRepository;
import com.example.myspringproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentResponse loadPayments(List<PaymentRequest> paymentRequest) {
        for (int i = 0; i < paymentRequest.size(); i++) {
            User user = userRepository.findByEmail(paymentRequest.get(i).employee().toLowerCase()).orElseThrow(() ->
                    new IllegalArgumentException("User not found."));

            if (paymentRequest.get(i).salary() < 0) {
                throw new IllegalArgumentException("Salary must be greater than 0");
            } else {
                LocalDate date = parsePeriod(paymentRequest.get(i).period());

                if (paymentRepository.findByUserAndPeriod(user, date).isPresent()) {
                    throw new IllegalArgumentException("Payment already exists for this user and period");
                } else {
                    Payment payment = Payment.builder()
                            .user(user)
                            .period(date)
                            .salary(paymentRequest.get(i).salary())
                            .build();
                    paymentRepository.save(payment);
                }
            }
        }

        return new PaymentResponse("Added successfully!");
    }

    public PaymentResponse updatePayments(PaymentRequest paymentRequest) {
        User user = userRepository.findByEmail(paymentRequest.employee().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        LocalDate date = parsePeriod(paymentRequest.period());
        if (paymentRequest.salary() < 0) {
            throw new IllegalArgumentException("Salary must be greater than 0");
        } else {
            Payment payment = paymentRepository.findByUserAndPeriod(user, date)
                    .orElseGet(Payment::new);
            payment.setUser(user);
            payment.setPeriod(date);
            payment.setSalary(paymentRequest.salary());
            paymentRepository.save(payment);
        }
        return new PaymentResponse("Updated successfully!");
    }

    public ResponseEntity<?> getDataOfSalaryForUser(Principal principal, String period) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (period != null) {
            LocalDate parsedPeriod = parsePeriod(period);
            Payment payment = paymentRepository.findByUserAndPeriod(user, parsedPeriod)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for the specified period."));

            return ResponseEntity.ok(new PaymentsResponseEmployee(
                    payment.getUser().getName(),
                    payment.getUser().getLastName(),
                    formatPeriod(payment.getPeriod()),
                    formatSalary(payment.getSalary())));
        } else {
            return ResponseEntity.ok(paymentRepository.findPaymentByUser(user).stream()
                    .sorted(Comparator.comparing(Payment::getPeriod).reversed())
                    .map(p -> new PaymentsResponseEmployee(
                            p.getUser().getName(),
                            p.getUser().getLastName(),
                            formatPeriod(p.getPeriod()),
                            formatSalary(p.getSalary())))
                    .toList());
        }
    }


    private String formatSalary(Long salary) {
        long dollars = salary / 100;
        long cents = salary % 100;
        return dollars + " dollar(s) " + cents + " cent(s)";
    }


    private LocalDate parsePeriod(String period) {
        if (period == null || period.isEmpty()) {
            throw new IllegalArgumentException("Period is required");
        } else if (!period.matches("(0[1-9]|1[0-2])-\\d{4}")) {
            throw new IllegalArgumentException("Invalid period format. Expected format: MM-YYYY");
        } else {
            String[] parts = period.split("-");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);

            return LocalDate.of(year, month, 1);
        }
    }

    private String formatPeriod(LocalDate period) {
        return period.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " - " + period.getYear();
    }
}

