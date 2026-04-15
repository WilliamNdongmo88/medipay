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

    public void notifyUser(String message, String senderName, Long receiverId, String type) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("senderName", senderName);
        payload.put("receiverId", String.valueOf(receiverId));
        payload.put("type", type); // "DEPOSIT" ou "PAYMENT"
        payload.put("timestamp", LocalDateTime.now().toString());

        // Envoie le message uniquement à l'utilisateur concerné
        //messagingTemplate.convertAndSendToUser(username, "/topic/notifications", payload);
        messagingTemplate.convertAndSend("/topic/notifications", payload);
    }
}
