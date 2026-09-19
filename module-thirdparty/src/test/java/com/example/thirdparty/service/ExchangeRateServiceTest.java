package com.example.thirdparty.service;

import com.example.thirdparty.dto.ExchangeRateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private HttpClient httpClient;

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        exchangeRateService = new ExchangeRateService(objectMapper);
        ReflectionTestUtils.setField(exchangeRateService, "httpClient", httpClient);
        ReflectionTestUtils.setField(exchangeRateService, "exchangeRateUrl", "https://api.frankfurter.dev/v1/latest");
        Thread.interrupted();
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
    }

    @Test
    void getExchangeRate_whenHttpResponseIs200AndRateExists_returnExchangeRateDto() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"amount\":1,\"base\":\"USD\",\"date\":\"2026-09-19\",\"rates\":{\"EUR\":0.91}}");
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ExchangeRateDto result = exchangeRateService.getExchangeRate("USD", "EUR");

        assertThat(result.getSourceCurrency()).isEqualTo("USD");
        assertThat(result.getTargetCurrency()).isEqualTo("EUR");
        assertThat(result.getRate()).isEqualByComparingTo(new BigDecimal("0.91"));
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 9, 19));
    }

    @Test
    void getExchangeRate_whenHttpStatusIsNot200_returnInternalServerError() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("upstream error");
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        assertThatThrownBy(() -> exchangeRateService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Unexpected error while getting exchange rate");
    }

    @Test
    void getExchangeRate_whenTargetRateMissing_returnInternalServerError() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"amount\":1,\"base\":\"USD\",\"date\":\"2026-09-19\",\"rates\":{\"JPY\":145.1}}");
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        assertThatThrownBy(() -> exchangeRateService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Unexpected error while getting exchange rate");
    }

    @Test
    void getExchangeRate_whenHttpClientThrowsIOException_returnInternalServerErrorWithIoMessage() throws Exception {
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("network down"));

        assertThatThrownBy(() -> exchangeRateService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Hit IOException while getting exchange rate");
    }

    @Test
    void getExchangeRate_whenHttpClientThrowsInterruptedException_returnInternalServerErrorAndInterruptedFlag() throws Exception {
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenThrow(new InterruptedException("interrupted"));

        assertThatThrownBy(() -> exchangeRateService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Hit InterruptedException while getting exchange rate");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    @Test
    void getExchangeRate_whenUnexpectedExceptionOccurs_returnInternalServerError() {
        ReflectionTestUtils.setField(exchangeRateService, "exchangeRateUrl", "://bad-url");

        assertThatThrownBy(() -> exchangeRateService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unexpected error while getting exchange rate");
    }
}
