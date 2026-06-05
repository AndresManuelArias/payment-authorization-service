package com.payment.api.domain.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Payment {
    private final String transactionId;
    private final String customerId;
    private final BigDecimal amount;
    private final String currency;
    private final String merchantId;
    private final String paymentMethod;
    private String status;
    private String authorizationCode;
    private String rejectionReason;

    public void approve(String authorizationCode) {
        this.status = "APPROVED";
        this.authorizationCode = authorizationCode;
    }

    public void reject(String reason) {
        this.status = "REJECTED";
        this.rejectionReason = reason;
    }
}