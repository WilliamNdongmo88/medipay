package com.medipay.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletResponse {

    private Long id;

    private Long userId;        // 🔥 au lieu de User
    private String username;    // optionnel mais très utile

    private BigDecimal balance;

    private LocalDateTime lastUpdated;
}