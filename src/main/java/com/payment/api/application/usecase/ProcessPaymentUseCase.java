package com.payment.api.application.usecase;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.payment.api.domain.model.Payment;
import com.payment.api.domain.repository.FraudDetectorPort;
import com.payment.api.domain.service.AmountPositiveValidator;
import com.payment.api.domain.service.MaxAmountValidator;
import com.payment.api.domain.service.PaymentValidator;

@Service
public class ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentUseCase.class);

    private final FraudDetectorPort fraudDetectorPort;
    private final BigDecimal maxLimit;


    public ProcessPaymentUseCase(
            FraudDetectorPort fraudDetectorPort,
            @Value("${payment.rules.max-limit:100000}") BigDecimal maxLimit) {
        this.fraudDetectorPort = fraudDetectorPort;
        this.maxLimit = maxLimit;
    }

    public Payment execute(Payment payment) {
        log.info("[USE CASE] Iniciando procesamiento de pago para la transacción: {}", payment.getTransactionId());


        PaymentValidator amountValidator = new AmountPositiveValidator();
        PaymentValidator limitValidator = new MaxAmountValidator(maxLimit);
        
        amountValidator.setNext(limitValidator);


        log.info("[USE CASE] Aplicando reglas de validación locales para la transacción: {}", payment.getTransactionId());
        Optional<String> validationError = amountValidator.validate(payment);

        if (validationError.isPresent()) {
            String errorMsg = validationError.get();
            log.warn("[USE CASE] Transacción {} RECHAZADA por reglas locales: {}", payment.getTransactionId(), errorMsg);
            payment.reject(errorMsg);
            return payment;
        }

        log.info("[USE CASE] Transacción {} válida localmente. Evaluando riesgo de fraude...", payment.getTransactionId());
        String fraudRisk = fraudDetectorPort.checkFraudRisk(payment);
        log.info("[USE CASE] Proveedor Antifraude retornó: {} para transacción: {}", fraudRisk, payment.getTransactionId());

        if ("HIGH_RISK".equalsIgnoreCase(fraudRisk)) {
            payment.reject("Payment rejected by anti-fraud system: HIGH_RISK detected");
        } else if ("LOW_RISK".equalsIgnoreCase(fraudRisk)) {
            String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            payment.approve(authCode);
            log.info("[USE CASE] Transacción {} APROBADA exitosamente con código: {}", payment.getTransactionId(), authCode);
        } else if ("FALLBACK_REJECTED".equalsIgnoreCase(fraudRisk)) {
            payment.reject("Payment rejected: Anti-fraud provider unavailable or timed out. Safe fallback applied.");
        } else {
            payment.reject("Payment rejected: Unknown risk status applied");
        }
        return payment;
    }
}