package com.example.cardpayment.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/card-payment/1");
    }

    @Test
    void handleResponseStatusException_whenReasonExists_returnReasonInBody() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");

        ResponseEntity<GlobalExceptionHandler.ErrorBody> response = handler.handleResponseStatusException(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Payment not found");
        assertThat(response.getBody().path()).isEqualTo("/api/card-payment/1");
    }

    @Test
    void handleNotFoundException_whenInvoked_returnNotFoundStatus() {
        ResponseEntity<GlobalExceptionHandler.ErrorBody> response = handler.handleNotFoundException(new NotFoundException(), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    void handleHttpMessageNotReadableException_whenPayloadInvalid_returnBadRequest() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("bad json");

        ResponseEntity<GlobalExceptionHandler.ErrorBody> response = handler.handleHttpMessageNotReadableException(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid request");
    }

    @Test
    void handleMethodArgumentTypeMismatchException_whenTypeMismatchOccurs_returnBadRequest() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc",
                Integer.class,
                "page",
                null,
                new IllegalArgumentException("invalid"));

        ResponseEntity<GlobalExceptionHandler.ErrorBody> response = handler.handleMethodArgumentTypeMismatchException(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid argument: page");
    }

    @Test
    void handleException_whenUnexpectedErrorOccurs_returnInternalServerError() {
        ResponseEntity<GlobalExceptionHandler.ErrorBody> response = handler.handleException(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
    }
}
