package com.medipay.functional;

import com.medipay.dto.CreditRequest;
import com.medipay.dto.LoginRequest;
import com.medipay.dto.SignupRequest;
import com.medipay.entity.User;
import com.medipay.enums.Role;
import com.medipay.repository.UserRepository;
import com.medipay.service.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerFT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email, String pseudo, Role userRole) throws Exception {
        SignupRequest signup = new SignupRequest();
        signup.setUsername(pseudo);
        signup.setEmail(email);
        signup.setPassword("123456");
        signup.setRole(userRole);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utilisateur enregistré avec succès !"));

        // récupérer le vrai user sauvegardé
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Test
    void should_credit_client_when_admin() throws Exception {
        // 1 créer utilisateur admin
        User admin = createUser("admin@mail.com", "admin", Role.ROLE_ADMIN);

        GrantedAuthority authority = new SimpleGrantedAuthority(admin.getRole().name());
        // 2 créer UserDetailsImpl réel
        UserDetailsImpl userDetails = new UserDetailsImpl(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getPassword(),
                Collections.singletonList(authority)
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // 1 créer utilisateur client
        User client = createUser("client@mail.com", "client", Role.ROLE_CLIENT);

        CreditRequest creditRequest = new CreditRequest();
        creditRequest.setUserId(client.getId());
        creditRequest.setAmount(BigDecimal.valueOf(5000));

        mockMvc.perform(post("/api/admin/credit")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creditRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Compte crédité"));
    }
}
