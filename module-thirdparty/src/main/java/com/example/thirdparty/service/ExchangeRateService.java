package com.example.thirdparty.service;

import com.example.thirdparty.dto.ExchangeRateDto;
import com.example.thirdparty.response.ExchangeRateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class ExchangeRateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${third-party.exchange-rate-url}") 
    private String exchangeRateUrl;

    public ExchangeRateService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public ExchangeRateDto getExchangeRate(String sourceCurrency, String targetCurrency) {
        try {
            String url = String.format("%s?from=%s&to=%s",
                    exchangeRateUrl,
                    URLEncoder.encode(sourceCurrency, StandardCharsets.UTF_8),
                    URLEncoder.encode(targetCurrency, StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            LOGGER.info("[Third-Party Request] method=GET url={}", url);

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.info("[Third-Party Response] method=GET url={} status={} body={}", url, httpResponse.statusCode(), httpResponse.body());

            if (httpResponse.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get exchange rate");
            }

            ExchangeRateResponse response = objectMapper.readValue(httpResponse.body(), ExchangeRateResponse.class);
            if (response == null || response.rates() == null || !response.rates().containsKey(targetCurrency)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No exchange rate returned");
            }
            return ExchangeRateDto.toDto(response, targetCurrency);
        } catch (IOException ioException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Hit IOException while getting exchange rate", ioException);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Hit InterruptedException while getting exchange rate", interruptedException);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while getting exchange rate", exception);
        }
    }

}
