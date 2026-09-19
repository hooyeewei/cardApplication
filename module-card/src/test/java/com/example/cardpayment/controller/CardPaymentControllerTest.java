package com.example.cardpayment.controller;

import com.example.cardpayment.dto.CardPaymentDto;
import com.example.cardpayment.enums.PaymentStatus;
import com.example.cardpayment.handler.GlobalExceptionHandler;
import com.example.cardpayment.request.CreatePaymentRequest;
import com.example.cardpayment.request.UpdatePaymentRequest;
import com.example.cardpayment.service.CardPaymentService;
import com.example.thirdparty.dto.ExchangeRateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardPaymentControllerTest {

    private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private CardPaymentService cardPaymentService;

        @BeforeEach
        void setUp() {
                cardPaymentService = mock(CardPaymentService.class);
                CardPaymentController controller = new CardPaymentController(cardPaymentService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

    @Test
        void createPayment_whenRequestIsValid_returnCreatedResponse() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "MREF-001",
                new BigDecimal("100.00"),
                com.example.cardpayment.enums.Currency.USD,
                "tok_123",
                "1234");

        when(cardPaymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(samplePaymentDto(1L));

        mockMvc.perform(post("/api/card-payment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.merchantReference").value("MREF-001"));
    }

    @Test
        void updatePayment_whenRequestIsValid_returnOkResponse() throws Exception {
        UpdatePaymentRequest request = new UpdatePaymentRequest(
                new BigDecimal("120.00"),
                com.example.cardpayment.enums.Currency.USD,
                PaymentStatus.CAPTURED);

        when(cardPaymentService.updatePayment(eq(1L), any(UpdatePaymentRequest.class))).thenReturn(samplePaymentDto(1L));

        mockMvc.perform(put("/api/card-payment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
        void getPayment_whenPaymentExists_returnOkResponse() throws Exception {
        when(cardPaymentService.getPayment(1L)).thenReturn(samplePaymentDto(1L));

        mockMvc.perform(get("/api/card-payment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
        void getPaymentList_whenPageIsRequested_returnPagedResponse() throws Exception {
        when(cardPaymentService.getPaymentlist(0)).thenReturn(new PageImpl<>(List.of(samplePaymentDto(1L), samplePaymentDto(2L))));

        mockMvc.perform(get("/api/card-payment/list").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
        void getExchangeRate_whenTargetCurrencyIsProvided_returnQuoteResponse() throws Exception {
        CardPaymentDto payment = samplePaymentDto(1L);
        ExchangeRateDto quote = new ExchangeRateDto("USD", "EUR", new BigDecimal("0.90"), LocalDate.of(2026, 9, 19));

        when(cardPaymentService.getPayment(1L)).thenReturn(payment);
        when(cardPaymentService.getExchangeRate(payment, "EUR")).thenReturn(quote);

        mockMvc.perform(get("/api/card-payment/1/exchange-rate").param("targetCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"));
    }

    @Test
        void getPayment_whenPaymentNotFound_returnReasonFromHandler() throws Exception {
        when(cardPaymentService.getPayment(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        mockMvc.perform(get("/api/card-payment/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found"))
                .andExpect(jsonPath("$.path").value("/api/card-payment/1"));
    }

    private CardPaymentDto samplePaymentDto(Long id) {
        Instant now = Instant.parse("2026-09-19T10:00:00Z");
        return new CardPaymentDto(
                id,
                "MREF-001",
                new BigDecimal("100.00"),
                "USD",
                "tok_123",
                "**** **** **** 1234",
                PaymentStatus.PENDING,
                now,
                now,
                1L);
    }
}
