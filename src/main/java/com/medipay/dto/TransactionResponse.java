package com.medipay.dto;

import com.medipay.entity.Wallet;
import com.medipay.enums.TransactionStatus;
import com.medipay.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private LocalDateTime timestamp;
    private String senderName;
    private String receiverName;
    private BigDecimal senderBalance;
    private BigDecimal receiverBalance;
}
