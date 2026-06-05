package com.payment.api.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class PaymentRequest {

    @NotBlank(message = "El transactionId es obligatorio")
    private String transactionId;

    @NotBlank(message = "El customerId es obligatorio")
    private String customerId;

    @NotNull(message = "El monto (amount) es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    @NotBlank(message = "La moneda (currency) es obligatorio")
    private String currency;

    @NotBlank(message = "El merchantId es obligatorio")
    private String merchantId;

    @NotBlank(message = "El método de pago (paymentMethod) es obligatorio")
    private String paymentMethod;
}