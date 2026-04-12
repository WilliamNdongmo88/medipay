package com.medipay.service;

import com.medipay.dto.UserResponse;
import com.medipay.entity.Wallet;
import com.medipay.repository.UserRepository;
import com.medipay.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public List<UserResponse> getAllUsersForAdmin() {
        return userRepository.findAll().stream().map(user -> {
            UserResponse dto = new UserResponse();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            dto.setCreationDate(user.getCreatedAt());

            // Récupération du solde depuis le Wallet lié
            Optional<Wallet> wallet = walletRepository.findByUserId(user.getId());
            if (wallet.get().getBalance() != null) {
                dto.setWalletBalance(wallet.get().getBalance());
            } else {
                dto.setWalletBalance(BigDecimal.valueOf(0.0));
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
