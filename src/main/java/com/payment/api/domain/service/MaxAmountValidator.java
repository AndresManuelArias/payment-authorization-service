package com.payment.api.domain.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.payment.api.domain.model.Payment;

public class MaxAmountValidator implements PaymentValidator {
    private PaymentValidator next;
    private final BigDecimal maxLimit;

    public MaxAmountValidator(BigDecimal maxLimit) {
        this.maxLimit = maxLimit;
    }

    @Override
    public void setNext(PaymentValidator nextValidator) {
        this.next = nextValidator;
    }

    @Override
    public Optional<String> validate(Payment payment) {
        if (payment.getAmount().compareTo(maxLimit) > 0) {
            return Optional.of("Payment rejected by business rules: Amount exceeds maximum limit allowable");
        }
        return next != null ? next.validate(payment) : Optional.empty();
    }
}