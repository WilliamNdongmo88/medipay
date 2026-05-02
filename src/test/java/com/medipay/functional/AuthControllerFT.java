package com.medipay.functional;


import com.jayway.jsonpath.JsonPath;
import com.medipay.dto.*;
import com.medipay.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerFT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void createUser(String email, String pseudo) throws Exception {
        SignupRequest signup = new SignupRequest();
        signup.setUsername(pseudo);
        signup.setEmail(email);
        signup.setPassword("123456");
        signup.setRole(Role.ROLE_CLIENT);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utilisateur enregistré avec succès !"));

    }

    // ✅ SIGNUP
    @Test
    void shouldRegisterUser() throws Exception {

        createUser("test@mail.com", "test");

    }

    // ✅ SIGNIN
    @Test
    void shouldLoginUser() throws Exception {

        createUser("test1@mail.com", "test1");

        LoginRequest request = new LoginRequest();
        request.setUsername("test1");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    // ✅ REFRESH TOKEN
    @Test
    void shouldRefreshToken() throws Exception {

        // 1️⃣ login pour récupérer un vrai token
        LoginRequest login = new LoginRequest();
        login.setUsername("test");
        login.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();

        String refreshToken = JsonPath.read(response, "$.refreshToken");

        // 2️⃣ utiliser ce vrai refresh token
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    // ✅ VERIFY EMAIL
    @Test
    void shouldVerifyEmail() throws Exception {

        // 1️⃣ créer un user (via signup)
        createUser("test2@mail.com", "test2");

        // 2️⃣ tester verify-email
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("test2@mail.com");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404IfEmailNotFound() throws Exception {

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("unknown@mail.com");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ✅ RESET PASSWORD
    @Test
    void shouldResetPassword() throws Exception {

        createUser("ndon@mail.com", "ndon");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("ndon@mail.com");
        request.setNewPassword("newpass");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ✅ RESET PASSWORD
    @Test
    void shouldFailResetPasswordIfUserNotFound() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("unknown@mail.com");
        request.setNewPassword("newpass");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
