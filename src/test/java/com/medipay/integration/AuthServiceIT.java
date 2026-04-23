package com.medipay.integration;


import com.medipay.dto.LoginRequest;
import com.medipay.dto.AuthResponse;
import com.medipay.dto.RefreshRequest;
import com.medipay.dto.VerifyEmailRequest;
import com.medipay.dto.ResetPasswordRequest;
import com.medipay.dto.SignupRequest;
import com.medipay.entity.User;
import com.medipay.enums.Role;
import com.medipay.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 🔥 désactive la sécurité pour les tests
class AuthServiceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void clean() {
        userRepository.deleteAll();
    }

    // =========================
    // ✅ SIGNUP
    // =========================
    @Test
    void shouldRegisterUser() throws Exception {

        SignupRequest request = new SignupRequest();
        request.setEmail("test3@mail.com");
        request.setPassword("123456");
        request.setUsername("test3");
        request.setRole(Role.ROLE_CLIENT);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());

        // Vérification DB
        Optional<User> user = userRepository.findByEmail("test3@mail.com");
        assertTrue(user.isPresent());
    }

    // =========================
    // ✅ SIGNIN
    // =========================
    @Test
    void shouldLoginUser() throws Exception {

        // 1️⃣ créer user via API (important pour password encodé)
        shouldRegisterUser();

        LoginRequest request = new LoginRequest();
        request.setUsername("test3");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    // =========================
    // ✅ REFRESH TOKEN
    // =========================
    @Test
    void shouldRefreshToken() throws Exception {

        // login pour récupérer refresh token réel
        shouldRegisterUser();

        LoginRequest login = new LoginRequest();
        login.setUsername("test3");
        login.setPassword("123456");

        String response = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(authResponse.getRefreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    // =========================
    // ✅ VERIFY EMAIL
    // =========================
    @Test
    void shouldVerifyEmail() throws Exception {

        shouldRegisterUser();

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("test3@mail.com");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email vérifié avec succès."));
    }

    @Test
    void shouldReturn404WhenEmailNotExists() throws Exception {

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("notfound@mail.com");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // =========================
    // ✅ RESET PASSWORD
    // =========================
    @Test
    void shouldResetPassword() throws Exception {

        shouldRegisterUser();

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test3@mail.com");
        request.setNewPassword("new123456");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mot de passe réinitialisé avec succès !"));
    }

    @Test
    void shouldReturn404WhenResetPasswordUserNotFound() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("unknown@mail.com");
        request.setNewPassword("123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
