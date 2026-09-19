package com.example.thirdparty.dto;

import com.example.thirdparty.response.ExchangeRateResponse;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExchangeRateDto {
    private final String sourceCurrency;
    private final String targetCurrency;
    private final BigDecimal rate;
    private final LocalDate date;

    public ExchangeRateDto(
        String sourceCurrency, 
        String targetCurrency, 
        BigDecimal rate, 
        LocalDate date) 
    {
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.date = date;
    }

    public static ExchangeRateDto toDto(ExchangeRateResponse quote, String targetCurrency) {
        return new ExchangeRateDto(
            quote.base(),
            targetCurrency,
            quote.rates().get(targetCurrency),
            quote.date());
    }
}