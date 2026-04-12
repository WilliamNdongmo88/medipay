package com.medipay.controller;

import com.medipay.dto.CreditRequest;
import com.medipay.dto.UserResponse;
import com.medipay.entity.Transaction;
import com.medipay.service.PaymentService;
import com.medipay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin" )
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/credit")
    public ResponseEntity<?> creditClient(@RequestBody CreditRequest creditRequest) {
        Transaction transaction = paymentService.creditClient(
                creditRequest.getUserId(),
                creditRequest.getAmount()
        );
        return ResponseEntity.ok(Map.of("message", "Compte crédité", "transactionId", transaction.getId()));
    }

    @PostMapping("/reset-pharmacist/{id}")
    public ResponseEntity<?> resetPharmacist(@PathVariable Long id) {
        Transaction transaction = paymentService.resetPharmacistBalance(id);
        return ResponseEntity.ok(Map.of("message", "Solde réinitialisé", "transactionId", transaction.getId()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsersForAdmin();
        return ResponseEntity.ok(users);
    }
}

