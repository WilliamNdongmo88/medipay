package com.medipay.functional;

import com.medipay.dto.PaymentRequest;
import com.medipay.dto.SignupRequest;
import com.medipay.entity.QRCode;
import com.medipay.entity.User;
import com.medipay.entity.Wallet;
import com.medipay.enums.Role;
import com.medipay.mapper.UserMapper;
import com.medipay.repository.QRCodeRepository;
import com.medipay.repository.UserRepository;
import com.medipay.repository.WalletRepository;
import com.medipay.service.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
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
import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerFT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private WalletRepository walletRepository;

    @AfterEach
    void clean() {
        qrCodeRepository.deleteAll();
        userRepository.deleteAll();
        walletRepository.deleteAll();
    }

    // création utilisateur
    private User createUser(String email, String pseudo, Role role) throws Exception {

        SignupRequest user = new SignupRequest();
        user.setUsername(pseudo);
        user.setEmail(email);
        user.setPassword("123456");
        user.setRole(role);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // récupérer le vrai user sauvegardé
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // dépôt dans wallet
    private void creditWallet(User user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(amount);
        walletRepository.save(wallet);
    }

    // création QR Code
    private QRCode createQRCode(User pharmacist) {
        QRCode qr = new QRCode();
        qr.setCodeValue("QR123");
        qr.setAmount(BigDecimal.valueOf(5000));
        qr.setPharmacist(pharmacist);
        qr.setUsed(false);
        qr.setCreatedAt(LocalDateTime.now());
        return qrCodeRepository.save(qr);
    }

    // TEST PAYMENT SUCCESS
    @Test
    void shouldProcessPayment() throws Exception {

        // 1 créer utilisateur client
        User client = createUser("client@mail.com", "client", Role.ROLE_CLIENT);

        GrantedAuthority authority = new SimpleGrantedAuthority(client.getRole().name());
        // 2 créer UserDetailsImpl réel
        UserDetailsImpl userDetails = new UserDetailsImpl(
                client.getId(),
                client.getUsername(),
                client.getEmail(),
                client.getPassword(),
                Collections.singletonList(authority)
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // dépot de 10000 dans wallet
        creditWallet(client, BigDecimal.valueOf(10000));

        // créer pharmacien
        User pharmacist = createUser("pharma@mail.com", "pharma", Role.ROLE_PHARMACIST);

        // créer QR Code
        createQRCode(pharmacist);

        // requête
        PaymentRequest request = new PaymentRequest();
        request.setQrCodeValue("QR123");

        mockMvc.perform(post("/api/payment/scan")
                        .with(authentication(auth)) // simulation user connecté
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Paiement effectué"))
                .andExpect(jsonPath("$.transactionId").exists());
    }

    // QR CODE INVALID
    @Test
    void shouldFailIfQRCodeInvalid() throws Exception {

        createUser("client2@mail.com", "client2", Role.ROLE_CLIENT);

        PaymentRequest request = new PaymentRequest();
        request.setQrCodeValue("INVALID");

        mockMvc.perform(post("/api/payment/scan")
                        .with(user("client"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // TEST PAYMENT SUCCESS
    @Test
    void shouldProcessPaymentStatic() throws Exception{
        // 1 créer utilisateur client
        User client = createUser("client3@mail.com", "client3", Role.ROLE_CLIENT);

        GrantedAuthority authority = new SimpleGrantedAuthority(client.getRole().name());
        // 2 créer UserDetailsImpl réel
        UserDetailsImpl userDetails = new UserDetailsImpl(
                client.getId(),
                client.getUsername(),
                client.getEmail(),
                client.getPassword(),
                Collections.singletonList(authority)
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
        );

        // dépot de 10000 dans wallet
        creditWallet(client, BigDecimal.valueOf(10000));

        // créer pharmacien
        User pharmacist = createUser("pharm@mail.com", "pharm", Role.ROLE_PHARMACIST);

        // requête
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(3500));
        request.setPharmacistId(pharmacist.getId());

        mockMvc.perform(post("/api/payment/pay-open")
                        .with(authentication(auth)) // simulation user connecté
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Paiement effectué"))
                .andExpect(jsonPath("$.transactionId").exists());

    }
}
