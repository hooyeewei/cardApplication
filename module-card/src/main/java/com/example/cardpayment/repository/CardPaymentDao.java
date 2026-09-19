package com.example.cardpayment.repository;

import com.example.cardpayment.entity.CardPaymentVo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardPaymentDao extends JpaRepository<CardPaymentVo, Long> {
}
