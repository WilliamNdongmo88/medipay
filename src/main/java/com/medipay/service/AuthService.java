package com.medipay.service;

import com.medipay.entity.User;
import com.medipay.entity.Wallet;
import com.medipay.enums.Role;
import com.medipay.repository.UserRepository;
import com.medipay.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String username, String email, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Erreur: Le pseudo est déjà pris !");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Erreur: L'email est déjà utilisé !");
        }

        // 1. Création de l'utilisateur
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        // 2. Initialisation du Wallet associé
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        return savedUser;
    }
}

