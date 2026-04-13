package com.medipay.mapper;

import com.medipay.dto.WalletResponse;
import com.medipay.entity.Wallet;

public class WalletMapper {

    public WalletResponse toDto(Wallet wallet) {
        if (wallet == null) return null;

        WalletResponse dto = new WalletResponse();
        dto.setId(wallet.getId());
        dto.setBalance(wallet.getBalance());
        dto.setLastUpdated(wallet.getLastUpdated());

        if (wallet.getUser() != null) {
            dto.setUserId(wallet.getUser().getId());
            dto.setUsername(wallet.getUser().getUsername());
        }

        return dto;
    }
}
