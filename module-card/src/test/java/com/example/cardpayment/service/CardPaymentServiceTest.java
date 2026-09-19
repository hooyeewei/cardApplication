package com.example.cardpayment.service;

import com.example.cardpayment.dto.CardPaymentDto;
import com.example.cardpayment.entity.CardPaymentVo;
import com.example.cardpayment.enums.Currency;
import com.example.cardpayment.enums.PaymentStatus;
import com.example.cardpayment.repository.CardPaymentDao;
import com.example.cardpayment.request.CreatePaymentRequest;
import com.example.cardpayment.request.UpdatePaymentRequest;
import com.example.thirdparty.dto.ExchangeRateDto;
import com.example.thirdparty.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardPaymentServiceTest {

    @Mock
    private CardPaymentDao cardPaymentDao;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private CardPaymentService cardPaymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cardPaymentService, "pageSize", 5);
    }

    @Test
    void createPayment_whenRequestIsValid_returnSavedPaymentWithMaskedCardNumber() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "MREF-001",
                new BigDecimal("100.00"),
                Currency.USD,
                "tok_123",
                "1234");

        CardPaymentVo saved = samplePaymentVo(1L);
        when(cardPaymentDao.save(any(CardPaymentVo.class))).thenReturn(saved);

        CardPaymentDto result = cardPaymentService.createPayment(request);

        ArgumentCaptor<CardPaymentVo> captor = ArgumentCaptor.forClass(CardPaymentVo.class);
        verify(cardPaymentDao).save(captor.capture());
        assertThat(captor.getValue().getMaskedCardNumber()).isEqualTo("**** **** **** 1234");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void updatePayment_whenPaymentExists_returnUpdatedPayment() {
        UpdatePaymentRequest request = new UpdatePaymentRequest(new BigDecimal("120.00"), Currency.EUR, PaymentStatus.CAPTURED);
        CardPaymentVo payment = samplePaymentVo(1L);

        when(cardPaymentDao.findById(1L)).thenReturn(Optional.of(payment));
        when(cardPaymentDao.save(any(CardPaymentVo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardPaymentDto result = cardPaymentService.updatePayment(1L, request);

        assertThat(result.getAmount()).isEqualByComparingTo("120.00");
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void getPayment_whenPaymentMissing_returnNotFoundException() {
        when(cardPaymentDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardPaymentService.getPayment(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void getPaymentList_whenPageRequested_returnPageUsingConfiguredSizeAndSorting() {
        when(cardPaymentDao.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(samplePaymentVo(1L))));

        Page<CardPaymentDto> result = cardPaymentService.getPaymentlist(2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardPaymentDao).findAll(captor.capture());
        Pageable pageable = captor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getExchangeRate_whenTargetCurrencyProvided_returnExchangeRateFromService() {
        CardPaymentDto payment = CardPaymentDto.toDto(samplePaymentVo(1L));
        ExchangeRateDto expected = new ExchangeRateDto("USD", "EUR", new BigDecimal("0.90"), LocalDate.of(2026, 9, 19));

        when(exchangeRateService.getExchangeRate(eq("USD"), eq("EUR"))).thenReturn(expected);

        ExchangeRateDto result = cardPaymentService.getExchangeRate(payment, "EUR");

        assertThat(result).isSameAs(expected);
    }

    private CardPaymentVo samplePaymentVo(Long id) {
        CardPaymentVo vo = new CardPaymentVo(
                "MREF-001",
                new BigDecimal("100.00"),
                "USD",
                "tok_123",
                "**** **** **** 1234");
        vo.setId(id);
        return vo;
    }
}
