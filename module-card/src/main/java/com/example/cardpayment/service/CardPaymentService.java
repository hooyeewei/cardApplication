package com.example.cardpayment.service;

import com.example.cardpayment.dto.CardPaymentDto;
import com.example.cardpayment.entity.CardPaymentVo;
import com.example.cardpayment.repository.CardPaymentDao;
import com.example.cardpayment.request.CreatePaymentRequest;
import com.example.cardpayment.request.UpdatePaymentRequest;
import com.example.thirdparty.dto.ExchangeRateDto;
import com.example.thirdparty.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CardPaymentService {

    private static final String MASKED_CARD_NUMBER_PREFIX = "**** **** **** ";

    @Value("${pagination.page.size}") 
    private int pageSize;

    private final CardPaymentDao cardPaymentDao;
    private final ExchangeRateService exchangeRateService;

    public CardPaymentService(CardPaymentDao cardPaymentDao, ExchangeRateService exchangeRateService) {
        this.cardPaymentDao = cardPaymentDao;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    public CardPaymentDto createPayment(CreatePaymentRequest request) {
        CardPaymentVo payment = new CardPaymentVo(
                request.merchantReference(),
                request.amount(),
                request.currency().name(),
                request.cardToken(),
                MASKED_CARD_NUMBER_PREFIX + request.lastFour());
        return CardPaymentDto.toDto(cardPaymentDao.save(payment));
    }

    @Transactional
    public CardPaymentDto updatePayment(Long id, UpdatePaymentRequest request) {
        CardPaymentVo payment = findCardPaymentById(id);
        payment.update(request.amount(), request.currency().name(), request.status());
        return CardPaymentDto.toDto(cardPaymentDao.save(payment));
    }

    @Transactional(readOnly = true)
    public CardPaymentDto getPayment(Long id) {
        return CardPaymentDto.toDto(findCardPaymentById(id));
    }

    @Transactional(readOnly = true)
    public Page<CardPaymentDto> getPaymentlist(int page) {
        return cardPaymentDao.findAll(PageRequest.of(
                page,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(CardPaymentDto::toDto);
    }

    public ExchangeRateDto getExchangeRate(CardPaymentDto paymentDto, String targetCurrency) {
        return exchangeRateService.getExchangeRate(paymentDto.getCurrency(), targetCurrency);
    }

    private CardPaymentVo findCardPaymentById(Long id) {
        return cardPaymentDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found", new Exception("Payment not found")));
    }

}
