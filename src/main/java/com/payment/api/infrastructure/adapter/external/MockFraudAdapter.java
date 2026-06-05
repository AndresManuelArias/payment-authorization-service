package com.payment.api.infrastructure.adapter.external;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.payment.api.domain.model.Payment;
import com.payment.api.domain.repository.FraudDetectorPort;

@Component
public class MockFraudAdapter implements FraudDetectorPort {

    @Override
    public String checkFraudRisk(Payment payment) {

        if (payment.getAmount().compareTo(new BigDecimal("999")) == 0) {
            return "HIGH_RISK";
        }
        return "LOW_RISK";
    }
}