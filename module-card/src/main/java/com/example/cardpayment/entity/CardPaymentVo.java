package com.example.cardpayment.entity;

import com.example.cardpayment.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "t_trxn_card_payments")
@Getter
@Setter
public class CardPaymentVo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 80)
    public String merchantReference;

    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    @Column(nullable = false, length = 3)
    public String currency;

    @Column(nullable = false, length = 255)
    public String cardToken;

    @Column(nullable = false, length = 19)
    public String maskedCardNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public PaymentStatus status;

    @Column(nullable = false, updatable = false)
    public Instant createdAt;

    @Column(nullable = false)
    public Instant updatedAt;

    @Version
    public Long version;

    protected CardPaymentVo() {
    }

    public CardPaymentVo(String merchantReference, BigDecimal amount, String currency,
                       String cardToken, String maskedCardNumber) {
        this.merchantReference = merchantReference;
        this.amount = amount;
        this.currency = currency;
        this.cardToken = cardToken;
        this.maskedCardNumber = maskedCardNumber;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(BigDecimal amount, String currency, PaymentStatus status) {
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.updatedAt = Instant.now();
    }

}
