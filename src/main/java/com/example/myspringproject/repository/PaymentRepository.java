package com.example.myspringproject.repository;

import com.example.myspringproject.entity.Payment;
import com.example.myspringproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByUserAndPeriod
            (User user, LocalDate period);

    List<Payment> findPaymentByUser(
            User user);
}
