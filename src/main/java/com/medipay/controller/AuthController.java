package com.medipay.controller;

import com.medipay.config.BatchConfig;
import com.medipay.dto.*;
import com.medipay.entity.User;
import com.medipay.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth" )
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final BatchConfig batchConfig;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        User user = authService.registerUser(signupRequest);
        return ResponseEntity.ok(Map.of("message", "Utilisateur enregistré avec succès !", "userId", user.getId()));
    }

    @PostMapping("/signin")
    public AuthResponse authenticateUser(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }

    // 1. Vérifier si l'email existe
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request);
    }

    // 2. Mettre à jour le mot de passe
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/batch")
    public String testBatch() {
        batchConfig.runTransactionsJob();
        return "Batch exécuté avec succès";
    }
}

