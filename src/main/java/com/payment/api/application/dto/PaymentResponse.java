package com.payment.api.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private String transactionId;
    private String status;
    
    // Este campo solo se serializará si no es nulo (es decir, en APPROVED)
    private String authorizationCode; 
    
    private String message;
}