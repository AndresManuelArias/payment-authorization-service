package com.payment.api.infrastructure.adapter.rest;

import com.payment.api.application.dto.PaymentRequest;
import com.payment.api.application.usecase.ProcessPaymentUseCase;
import com.payment.api.domain.model.Payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessPaymentUseCase processPaymentUseCase;

    @Test
    @DisplayName("POST /api/payments/authorize - Debería retornar HTTP 200 OK y JSON de aprobación")
    void authorizePaymentSuccess() throws Exception {
        PaymentRequest request = new PaymentRequest("TX-999", "CUST-1", new BigDecimal("1000"), "COP", "MER-1", "CARD");

        Payment mockProcessedPayment = Payment.builder()
                .transactionId("TX-999")
                .status("APPROVED")
                .authorizationCode("AUTH-123456")
                .build();

        when(processPaymentUseCase.execute(any(Payment.class))).thenReturn(mockProcessedPayment);

        mockMvc.perform(post("/api/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TX-999"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.authorizationCode").value("AUTH-123456"))
                .andExpect(jsonPath("$.message").value("Payment authorized"));
    }

    @Test
    @DisplayName("POST /api/payments/authorize - Debería retornar HTTP 400 Bad Request cuando faltan campos obligatorios")
    void authorizePaymentValidationError() throws Exception {
        PaymentRequest corruptRequest = new PaymentRequest("", "CUST-1", new BigDecimal("-50"), "COP", "MER-1", "CARD");

        mockMvc.perform(post("/api/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corruptRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.amount").exists());

        verifyNoInteractions(processPaymentUseCase);
    }

    @Test
    @DisplayName("GET /api/payments/{transactionId} - Debería retornar HTTP 200 OK y los detalles de la transacción existente")
    void getPaymentByIdFound() throws Exception {
        PaymentRequest request = new PaymentRequest("TX-999", "CUST-1", new BigDecimal("1000"), "COP", "MER-1", "CARD");

        Payment mockProcessedPayment = Payment.builder()
                .transactionId("TX-999")
                .status("APPROVED")
                .authorizationCode("AUTH-123456")
                .build();

        when(processPaymentUseCase.execute(any(Payment.class))).thenReturn(mockProcessedPayment);

        mockMvc.perform(post("/api/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/payments/TX-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TX-999"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.authorizationCode").value("AUTH-123456"))
                .andExpect(jsonPath("$.message").value("Payment authorized"));
    }

    @Test
    @DisplayName("GET /api/payments/{transactionId} - Debería retornar HTTP 404 si la transacción no existe")
    void getPaymentByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/TX-UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
