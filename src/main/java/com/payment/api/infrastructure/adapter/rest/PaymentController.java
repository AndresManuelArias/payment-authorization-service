package com.payment.api.infrastructure.adapter.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.api.application.dto.PaymentRequest;
import com.payment.api.application.dto.PaymentResponse;
import com.payment.api.application.usecase.ProcessPaymentUseCase;
import com.payment.api.domain.model.Payment;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final ProcessPaymentUseCase processPaymentUseCase;
    
    private final Map<String, Payment> repositoryInMemory = new ConcurrentHashMap<>();

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> authorizePayment(@Valid @RequestBody PaymentRequest request) {
        log.info("[CONTROLLER] Solicitud de autorización recibida para transactionId: {}", request.getTransactionId());

        Payment paymentDomain = Payment.builder()
                .transactionId(request.getTransactionId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantId(request.getMerchantId())
                .paymentMethod(request.getPaymentMethod())
                .build();


        Payment processedPayment = processPaymentUseCase.execute(paymentDomain);

        repositoryInMemory.put(processedPayment.getTransactionId(), processedPayment);

        PaymentResponse response = PaymentResponse.builder()
                .transactionId(processedPayment.getTransactionId())
                .status(processedPayment.getStatus())
                .authorizationCode(processedPayment.getAuthorizationCode())
                .message("APPROVED".equals(processedPayment.getStatus()) ? "Payment authorized" : processedPayment.getRejectionReason())
                .build();

        HttpStatus status = "APPROVED".equals(processedPayment.getStatus()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String transactionId) {
        log.info("[CONTROLLER] Consultando detalles de la transacción: {}", transactionId);

        Payment payment = repositoryInMemory.get(transactionId);

        if (payment == null) {
            log.warn("[CONTROLLER] Transacción {} no encontrada.", transactionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        PaymentResponse response = PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .authorizationCode(payment.getAuthorizationCode())
                .message("APPROVED".equals(payment.getStatus()) ? "Payment authorized" : payment.getRejectionReason())
                .build();

        return ResponseEntity.ok(response);
    }
}