package com.payment.api.application.usecase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.payment.api.domain.model.Payment;
import com.payment.api.domain.repository.FraudDetectorPort;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentUseCaseTest {

    @Mock
    private FraudDetectorPort fraudDetectorPort;

    private ProcessPaymentUseCase processPaymentUseCase;
    private final BigDecimal maxLimit = new BigDecimal("150000");

    @BeforeEach
    void setUp() {
        
        processPaymentUseCase = new ProcessPaymentUseCase(fraudDetectorPort, maxLimit);
    }

    @Test
    @DisplayName("Debería APROBAR el pago cuando las reglas locales son válidas y el riesgo es LOW_RISK")
    void shouldApprovePaymentWhenRulesValidAndLowRisk() {
        
        Payment payment = createBasePayment("TX-100", new BigDecimal("50000"));
        when(fraudDetectorPort.checkFraudRisk(any(Payment.class))).thenReturn("LOW_RISK");


        Payment result = processPaymentUseCase.execute(payment);


        assertEquals("APPROVED", result.getStatus());
        assertNotNull(result.getAuthorizationCode());
        assertNull(result.getRejectionReason());
        verify(fraudDetectorPort, times(1)).checkFraudRisk(any(Payment.class));
    }

    @Test
    @DisplayName("Debería RECHAZAR el pago cuando el monto es menor o igual a cero")
    void shouldRejectPaymentWhenAmountIsZeroOrNegative() {

        Payment payment = createBasePayment("TX-101", new BigDecimal("0"));

        Payment result = processPaymentUseCase.execute(payment);

        assertEquals("REJECTED", result.getStatus());
        assertTrue(result.getRejectionReason().contains("Amount must be greater than zero"));
        verifyNoInteractions(fraudDetectorPort);
    }

    @Test
    @DisplayName("Debería RECHAZAR el pago cuando el monto supera el límite máximo permitido")
    void shouldRejectPaymentWhenAmountExceedsMaxLimit() {
        Payment payment = createBasePayment("TX-102", new BigDecimal("200000"));

        Payment result = processPaymentUseCase.execute(payment);

        assertEquals("REJECTED", result.getStatus());
        assertTrue(result.getRejectionReason().contains("Amount exceeds maximum limit"));
        verifyNoInteractions(fraudDetectorPort);
    }

    @Test
    @DisplayName("Debería RECHAZAR el pago cuando el proveedor externo detecta HIGH_RISK")
    void shouldRejectPaymentWhenFraudDetectorReturnsHighRisk() {
        Payment payment = createBasePayment("TX-103", new BigDecimal("50000"));
        when(fraudDetectorPort.checkFraudRisk(any(Payment.class))).thenReturn("HIGH_RISK");

        Payment result = processPaymentUseCase.execute(payment);

        assertEquals("REJECTED", result.getStatus());
        assertTrue(result.getRejectionReason().contains("HIGH_RISK detected"));
        verify(fraudDetectorPort, times(1)).checkFraudRisk(any(Payment.class));
    }

    @Test
    @DisplayName("Debería RECHAZAR de forma segura cuando el proveedor activa el Fallback controlado")
    void shouldApplySafeFallbackWhenFraudDetectorFails() {

        Payment payment = createBasePayment("TX-104", new BigDecimal("50000"));
        
        when(fraudDetectorPort.checkFraudRisk(any(Payment.class))).thenReturn("FALLBACK_REJECTED");

        Payment result = processPaymentUseCase.execute(payment);

        assertEquals("REJECTED", result.getStatus());
        assertTrue(result.getRejectionReason().contains("Anti-fraud provider unavailable or timed out"));
        verify(fraudDetectorPort, times(1)).checkFraudRisk(any(Payment.class));
    }

    private Payment createBasePayment(String txId, BigDecimal amount) {
        return Payment.builder()
                .transactionId(txId)
                .customerId("CUST-001")
                .amount(amount)
                .currency("COP")
                .merchantId("MER-900")
                .paymentMethod("CARD")
                .build();
    }
}