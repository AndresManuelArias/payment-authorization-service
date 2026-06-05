package com.payment.api.domain.repository;

import com.payment.api.domain.model.Payment;

public interface FraudDetectorPort {

    String checkFraudRisk(Payment payment);
}