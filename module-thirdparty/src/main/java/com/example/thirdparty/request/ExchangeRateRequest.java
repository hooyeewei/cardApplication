package com.example.thirdparty.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExchangeRateRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currencyFrom,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currencyTo) {
}