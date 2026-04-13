package com.medipay.service;

import com.medipay.dto.*;
import com.medipay.entity.RefreshToken;
import com.medipay.entity.User;
import com.medipay.entity.Wallet;
import com.medipay.enums.Role;
import com.medipay.mapper.UserMapper;
import com.medipay.repository.RefreshTokenRepository;
import com.medipay.repository.UserRepository;
import com.medipay.repository.WalletRepository;
import com.medipay.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;

    public List<UserResponse> processUser() {

        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = userMapper.toListUserResponseDto(users);

        // 🔥 envoi temps réel
        messagingTemplate.convertAndSend("/topic/users", userResponses);
        return userResponses;
    }

    @Transactional
    public User registerUser(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Erreur: Le pseudo est déjà pris !");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Erreur: L'email est déjà utilisé !");
        }

        // 1. Création de l'utilisateur
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        // 2. Initialisation du Wallet associé
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        processUser();
        //processUser(savedUser, wallet);
        return savedUser;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        System.out.println("userDetails: "+ userDetails);

        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        refreshTokenRepository.deleteByUserId(user.getId());

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String requestToken) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        String accessToken = jwtUtils.generateToken(refreshToken.getUser());

        return new AuthResponse(accessToken, requestToken);
    }

    @Transactional
    public ResponseEntity<?> verifyEmail(VerifyEmailRequest request){
        boolean exists = userRepository.existsByEmail(request.getEmail());

        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RuntimeException("Erreur : Aucun compte n'est associé à cet email."));
        }

        return ResponseEntity.ok(new RuntimeException("Email vérifié avec succès."));
    }

    @Transactional
    public ResponseEntity<?> resetPassword(ResetPasswordRequest request){
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RuntimeException("Erreur : Utilisateur introuvable."));
        }

        User user = userOptional.get();
        // Encodage du nouveau mot de passe avec BCrypt
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new RuntimeException("Mot de passe réinitialisé avec succès !"));
    }
}