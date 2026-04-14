package com.medipay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String qrCodeValue;
    private Long pharmacistId;
    private BigDecimal amount;
}
