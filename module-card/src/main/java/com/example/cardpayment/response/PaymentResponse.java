package com.example.cardpayment.response;

import com.example.cardpayment.dto.CardPaymentDto;
import com.example.cardpayment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        String merchantReference,
        BigDecimal amount,
        String currency,
        String maskedCardNumber,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt) 
{
    public static PaymentResponse toResponse(CardPaymentDto payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getMerchantReference(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getMaskedCardNumber(),
                    payment.getStatus(),
                    payment.getCreatedAt(),
                    payment.getUpdatedAt());
    }
}