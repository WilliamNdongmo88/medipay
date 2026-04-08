package com.medipay.config;

import com.medipay.enums.Role;
import com.medipay.repository.UserRepository;
import com.medipay.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Base de données vide. Initialisation de l'administrateur par défaut...");

            authService.registerUser(
                    "admin",
                    "williamndongmo899@gmail.com",
                    "admin123",
                    Role.ROLE_ADMIN
            );

            logger.info("Administrateur créé avec succès !");
            logger.info("Pseudo : admin | Mot de passe : admin123");
        }
    }
}
