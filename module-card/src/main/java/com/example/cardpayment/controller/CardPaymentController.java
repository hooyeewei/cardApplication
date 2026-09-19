package com.example.cardpayment.controller;

import com.example.cardpayment.dto.CardPaymentDto;
import com.example.cardpayment.enums.Currency;
import com.example.cardpayment.request.CreatePaymentRequest;
import com.example.cardpayment.request.UpdatePaymentRequest;
import com.example.cardpayment.response.PageResponse;
import com.example.cardpayment.response.PaymentExchangeRateResponse;
import com.example.cardpayment.response.PaymentResponse;
import com.example.cardpayment.service.CardPaymentService;
import com.example.thirdparty.dto.ExchangeRateDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card-payment")
public class CardPaymentController {

    private final CardPaymentService service;

    public CardPaymentController(CardPaymentService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        CardPaymentDto result = service.createPayment(request);
        return PaymentResponse.toResponse(result);
    }

    @PutMapping("/{id}")
    public PaymentResponse updatePayment(@PathVariable Long id, @Valid @RequestBody UpdatePaymentRequest request) {
        CardPaymentDto result = service.updatePayment(id, request);
        return PaymentResponse.toResponse(result);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        CardPaymentDto result = service.getPayment(id);
        return PaymentResponse.toResponse(result);
    }

    @GetMapping("/list")
    public PageResponse<PaymentResponse> getPaymentlist(@RequestParam(defaultValue = "0") @Min(0) int page) {
        Page<CardPaymentDto> result = service.getPaymentlist(page);
        return new PageResponse<>(
                result.getContent().stream().map(PaymentResponse::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @GetMapping("/{id}/exchange-rate")
    public PaymentExchangeRateResponse getExchangeRate(
            @PathVariable Long id,
            @RequestParam Currency targetCurrency) {
        CardPaymentDto payment = service.getPayment(id);
        ExchangeRateDto quote = service.getExchangeRate(payment, targetCurrency.name());
        return PaymentExchangeRateResponse.toResponse(payment, quote);
    }

}
