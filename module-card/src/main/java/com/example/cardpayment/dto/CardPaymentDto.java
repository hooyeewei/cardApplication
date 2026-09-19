package com.example.cardpayment.dto;

import com.example.cardpayment.entity.CardPaymentVo;
import com.example.cardpayment.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class CardPaymentDto {
    private final Long id;
    private final String merchantReference;
    private final BigDecimal amount;
    private final String currency;
    private final String cardToken;
    private final String maskedCardNumber;
    private final PaymentStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Long version;

    public CardPaymentDto(Long id,
                          String merchantReference,
                          BigDecimal amount,
                          String currency,
                          String cardToken,
                          String maskedCardNumber,
                          PaymentStatus status,
                          Instant createdAt,
                          Instant updatedAt,
                          Long version) {
        this.id = id;
        this.merchantReference = merchantReference;
        this.amount = amount;
        this.currency = currency;
        this.cardToken = cardToken;
        this.maskedCardNumber = maskedCardNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static CardPaymentDto toDto(CardPaymentVo payment) {
        return new CardPaymentDto(
                payment.getId(),
                payment.getMerchantReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCardToken(),
                payment.getMaskedCardNumber(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getVersion());
    }
}
