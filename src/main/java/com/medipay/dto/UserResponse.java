package com.medipay.dto;

import com.medipay.enums.Role;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private BigDecimal walletBalance; // Solde extrait du Wallet associé
    private LocalDateTime creationDate;
    private LocalDateTime lastLoginDate;
}
