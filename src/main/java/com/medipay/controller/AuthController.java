package com.medipay.controller;

import com.medipay.dto.AuthResponse;
import com.medipay.dto.LoginRequest;
import com.medipay.dto.RefreshRequest;
import com.medipay.dto.SignupRequest;
import com.medipay.entity.User;
import com.medipay.service.AuthService;
import com.medipay.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth" )
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

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
}

