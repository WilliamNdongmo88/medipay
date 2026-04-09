package com.medipay.service;

import com.medipay.dto.TransactionResponse;
import com.medipay.entity.Transaction;
import com.medipay.entity.User;
import com.medipay.enums.TransactionType;
import com.medipay.enums.TransactionStatus;
import com.medipay.entity.Wallet;
import com.medipay.mapper.TransactionMapper;
import com.medipay.repository.TransactionRepository;
import com.medipay.repository.UserRepository;
import com.medipay.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final WalletRepository walletRepository;
    private final AuthenticationManager authenticationManager;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    // 1. Créditer un client (Réservé à l'Admin)
    @Transactional
    public Transaction creditClient(Long userId, BigDecimal amount) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: "+ userDetails);

        Wallet senderWallet = walletRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Portefeuille non trouvé pour l'utilisateur ID: " + userId));


        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Portefeuille non trouvé pour l'utilisateur ID: " + userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(senderWallet);
        transaction.setReceiverWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription("Dépôt effectué par l'administrateur");

        return transactionRepository.save(transaction);
    }

    // 2. Effectuer un paiement (Client vers Pharmacien)
    @Transactional
    public Transaction processPayment(Long clientId, Long pharmacistId, BigDecimal amount, String qrCodeValue) {
        Wallet clientWallet = walletRepository.findByUserId(clientId)
                .orElseThrow(() -> new RuntimeException("Portefeuille client non trouvé"));

        Wallet pharmacistWallet = walletRepository.findByUserId(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Portefeuille pharmacien non trouvé"));

        // Vérification cruciale du solde
        if (clientWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Solde insuffisant pour effectuer ce paiement");
        }

        // Débit / Crédit atomique
        clientWallet.setBalance(clientWallet.getBalance().subtract(amount));
        pharmacistWallet.setBalance(pharmacistWallet.getBalance().add(amount));

        walletRepository.save(clientWallet);
        walletRepository.save(pharmacistWallet);

        // Enregistrement de la transaction
        Transaction transaction = new Transaction();
        transaction.setSenderWallet(clientWallet);
        transaction.setReceiverWallet(pharmacistWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription("Paiement pharmacie via QR Code: " + qrCodeValue);

        return transactionRepository.save(transaction);
    }

    // 3. Réinitialiser le solde d'un pharmacien (Admin)
    @Transactional
    public Transaction resetPharmacistBalance(Long pharmacistId) {
        Wallet wallet = walletRepository.findByUserId(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Portefeuille non trouvé"));

        BigDecimal oldBalance = wallet.getBalance();
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(wallet);
        transaction.setAmount(oldBalance);
        transaction.setType(TransactionType.RESET);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription("Réinitialisation du solde pharmacien");

        return transactionRepository.save(transaction);
    }

    public List<TransactionResponse> getUserHistory(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Portefeuille non trouvé"));

        List<Transaction> transactions = transactionRepository.findAllWithUsers(wallet);

        return transactionMapper.toResponseList(transactions);
    }
}

