package com.medipay.controller;

import com.medipay.dto.PaymentRequest;
import com.medipay.dto.TransactionResponse;
import com.medipay.dto.UserResponse;
import com.medipay.entity.QRCode;
import com.medipay.entity.Transaction;
import com.medipay.service.AuthService;
import com.medipay.service.PaymentService;
import com.medipay.service.QRCodeService;
import com.medipay.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment" )
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final QRCodeService qrCodeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthService authService;

    @PostMapping("/scan")
    public ResponseEntity<?> processPayment(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody PaymentRequest paymentRequest) {

        // Validation du QR Code
        QRCode qrCode = qrCodeService.validateAndGetQRCode(paymentRequest.getQrCodeValue());

        // Exécution du paiement
        Transaction transaction = paymentService.processPayment(
                currentUser.getId(),
                qrCode.getPharmacist().getId(),
                qrCode.getAmount(), // Ou le montant saisi si QR statique
                qrCode.getCodeValue()
        );

        return ResponseEntity.ok(Map.of("message", "Paiement effectué", "transactionId", transaction.getId()));
    }

    @PostMapping("/pay-open")
    public ResponseEntity<?> processPaymentStatic(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody PaymentRequest paymentRequest) {

        // Exécution du paiement
        Transaction transaction = paymentService.executeOpenPayment(
                currentUser.getId(),
                paymentRequest.getPharmacistId(),
                paymentRequest.getAmount()
        );

        return ResponseEntity.ok(Map.of("message", "Paiement effectué", "transactionId", transaction.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> getMyHistory(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        System.out.println("USER: " + SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(paymentService.getUserHistory(currentUser.getId()));
    }

    @GetMapping("/ws")
    public String testWs() {
        //paymentService.processTransaction();
        return "Message envoyé";
    }
}
