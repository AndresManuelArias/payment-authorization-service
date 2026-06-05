package com.payment.api.domain.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.payment.api.domain.model.Payment;

public class AmountPositiveValidator implements PaymentValidator {
    private PaymentValidator next;

    @Override
    public void setNext(PaymentValidator nextValidator) {
        this.next = nextValidator;
    }

    @Override
    public Optional<String> validate(Payment payment) {
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("Payment rejected: Amount must be greater than zero");
        }
        return next != null ? next.validate(payment) : Optional.empty();
    }
}