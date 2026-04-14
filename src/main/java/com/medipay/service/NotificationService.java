package com.medipay.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyUser(String username, String message, String type) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("type", type); // "DEPOSIT" ou "PAYMENT"
        payload.put("timestamp", LocalDateTime.now().toString());

        // Envoie le message uniquement à l'utilisateur concerné
        //messagingTemplate.convertAndSendToUser(username, "/topic/notifications", payload);
        messagingTemplate.convertAndSend("/topic/transactions", payload);
    }
}
