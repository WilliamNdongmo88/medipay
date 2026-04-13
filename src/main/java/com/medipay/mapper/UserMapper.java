package com.medipay.mapper;

import com.medipay.dto.SignupRequest;
import com.medipay.dto.UserResponse;
import com.medipay.entity.User;
import com.medipay.entity.Wallet;
import com.medipay.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    public User toEntity(SignupRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return user;
    }

    public static UserResponse toDto(User user, Wallet wallet) {
        if (user == null) {
            return null;
        }

        UserResponse dto = new UserResponse();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        // 🔥 Wallet lié
        if (wallet != null) {
            dto.setWalletBalance(wallet.getBalance());
        } else {
            dto.setWalletBalance(BigDecimal.ZERO); // ou null selon ton besoin
        }

        // Dates
        dto.setCreationDate(user.getCreatedAt());

        // ⚠️ si tu as ce champ dans User
//        dto.setLastLoginDate(user.getLastLoginDate());

        return dto;
    }

    public List<UserResponse> toListUserResponseDto(List<User> users){
        return users.stream().map(user -> {
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
                dto.setWalletBalance(BigDecimal.ZERO);
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
