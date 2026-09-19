package com.example.cardpayment.response;

import com.example.cardpayment.dto.CardPaymentDto;
import com.example.thirdparty.dto.ExchangeRateDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentExchangeRateResponse(
        Long paymentId,
        BigDecimal paymentAmount,
        BigDecimal convertedAmount,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal exchangeRate,
        LocalDate rateDate) 
{
        public static PaymentExchangeRateResponse toResponse(CardPaymentDto payment, ExchangeRateDto quote) {
            return new PaymentExchangeRateResponse(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getAmount().multiply(quote.getRate()),
                    quote.getSourceCurrency(),
                    quote.getTargetCurrency(),
                    quote.getRate(),
                    quote.getDate());
        }
}