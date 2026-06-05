package com.payment.api.domain.service;

import java.util.Optional;

import com.payment.api.domain.model.Payment;

public interface PaymentValidator {
    void setNext(PaymentValidator nextValidator);
    Optional<String> validate(Payment payment);
}