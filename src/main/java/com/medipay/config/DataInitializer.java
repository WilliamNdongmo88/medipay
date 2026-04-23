package com.medipay.config;

import com.medipay.dto.SignupRequest;
import com.medipay.enums.Role;
import com.medipay.repository.UserRepository;
import com.medipay.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${spring.profiles.active}")
    private String env;

    @Override
    public void run(String... args) {
        if(Objects.equals(env, "dev") || Objects.equals(env, "prod")){
            if (userRepository.count() == 0) {
                logger.info("Base de données vide. Initialisation de l'administrateur par défaut...");

                SignupRequest request = new SignupRequest();
                request.setUsername("williamndongmo");
                request.setEmail("williamndongmo899@gmail.com");
                request.setPassword("admin123");
                request.setRole(Role.ROLE_ADMIN);

                authService.registerUser(request);

                logger.info("Administrateur créé avec succès !");
                logger.info("Pseudo : williamndongmo | Mot de passe : admin123");
            }
        }
    }
}
