package com.payment.api.infrastructure.adapter.external;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.payment.api.domain.model.Payment;
import com.payment.api.domain.repository.FraudDetectorPort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@Component
public class MockFraudAdapter implements FraudDetectorPort {

    private static final Logger log = LoggerFactory.getLogger(MockFraudAdapter.class);

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "fraudCheckCache", key = "#payment.transactionId")
    @CircuitBreaker(name = "antiFraudProvider", fallbackMethod = "fallbackFraudCheck")
    @TimeLimiter(name = "antiFraudProvider")
    public String checkFraudRisk(Payment payment) {
        log.info("[ADAPTER - CACHE MISS] Consultando proveedor externo real para transacción: {}", payment.getTransactionId());
        if (payment.getAmount().compareTo(new BigDecimal("999")) == 0) {
            return "HIGH_RISK";
        }
        if (payment.getAmount().compareTo(new BigDecimal("5000")) == 0) {
            log.warn("[ADAPTER] Simulado retraso de red (Timeout) de 4 segundos para la transacción: {}", payment.getTransactionId());
            try {
                Thread.sleep(4000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


        if (payment.getAmount().compareTo(new BigDecimal("7777")) == 0) {
            log.error("[ADAPTER] Simulado error crítico del servidor del proveedor externo.");
            throw new RuntimeException("External Fraud Provider Server Error 500");
        }

        return "LOW_RISK";
    }

    public String fallbackFraudCheck(Payment payment, Throwable exception) {
        log.error("[RESILIENCE - FALLBACK] Activado fallback para la transacción: {}. Razón del fallo: {}", 
                payment.getTransactionId(), exception.getMessage());
        return "FALLBACK_REJECTED";
    }
}