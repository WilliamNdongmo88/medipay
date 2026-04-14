package com.medipay.service;

import com.medipay.dto.TransactionResponse;
import com.medipay.entity.Transaction;
import com.medipay.enums.TransactionType;
import com.medipay.enums.TransactionStatus;
import com.medipay.entity.Wallet;
import com.medipay.mapper.TransactionMapper;
import com.medipay.mapper.WalletMapper;
import com.medipay.repository.QRCodeRepository;
import com.medipay.repository.TransactionRepository;
import com.medipay.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final QRCodeRepository qrCodeRepository;
    private final NotificationService notificationService;

    @Transactional
    public void processTransaction(Transaction ts) {
        // sauvegarde en base
        transactionRepository.save(ts);
        List<Transaction> tx = transactionRepository.findAll();
        List<TransactionResponse> transactionResponses = transactionMapper.toResponseList(tx);

        // 🔥 envoi temps réel
        messagingTemplate.convertAndSend("/topic/transactions", transactionResponses);
    }

    @Transactional
    public void processTransactionWallet(Wallet wallet) {
        // sauvegarde en base
        List<Transaction> tx = transactionRepository.findByReceiverWalletId(wallet.getId());
        System.out.println("Taille de la liste: " + tx.size());
        List<TransactionResponse> transactionResponses = transactionMapper.toResponseList(tx);
        System.out.println("Taille de la liste: " + transactionResponses.size());

        // 🔥 envoi temps réel
        messagingTemplate.convertAndSend("/topic/wallets", transactionResponses);
    }

    // 1. Créditer un client fermé (Réservé à l'Admin)
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

        //processTransactionWallet(wallet); // Déclenche le WebSocket

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(senderWallet);
        transaction.setReceiverWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription("Dépôt effectué avec succès."+
                "`\n Expediteur: "+ senderWallet.getUser().getUsername()+
                ",\n Bénéficiaire: " + wallet.getUser().getUsername()
        );

        notificationService.notifyUser(wallet.getUser().getUsername(),
                        "Votre compte a été crédité de"+ amount+ "XAF", "DEPOSIT");
        processTransaction(transaction);// Déclenche le WebSocket
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
        transaction.setDescription("Paiement pharmacie via QR Code. "+
                        "\n Expediteur: " + clientWallet.getUser().getUsername()+
                ",\n Bénéficiaire: " + pharmacistWallet.getUser().getUsername()
        );

        notificationService.notifyUser(pharmacistWallet.getUser().getUsername(),
                "Nouveau paiement reçu de "+ amount+ "XAF", "PAYMENT");
        markAsUsed(qrCodeValue); // Marque le qrCode comme déjà utilisé
        processTransaction(transaction); // Déclenche le WebSocket
        return transactionRepository.save(transaction);
    }

    // 2. Effectuer un paiement ouvert (Client vers Pharmacien)
    @Transactional
    public Transaction executeOpenPayment(Long patientId, Long pharmacistId, BigDecimal amount) {
        // 1. Validation de sécurité de base
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro.");
        }

        // 2. Récupération des portefeuilles
        Optional<Wallet> patientWallet = walletRepository.findByUserId(patientId);
        Optional<Wallet> pharmacistWallet = walletRepository.findByUserId(pharmacistId);

        // 3. Vérification du solde du patient
        if (patientWallet.get().getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Solde insuffisant pour effectuer ce paiement.");
        }

        // 4. Transfert atomique
        patientWallet.get().setBalance(
                patientWallet.get().getBalance().subtract(amount)
        );
        pharmacistWallet.get().setBalance(
                pharmacistWallet.get().getBalance().add(amount)
        );

        //processTransactionWallet(pharmacistWallet.get()); // Déclenche le WebSocket

        // 5. Création de la transaction
        Transaction tx = new Transaction();
        tx.setSenderWallet(patientWallet.get());
        tx.setReceiverWallet(pharmacistWallet.get());
        tx.setAmount(amount);
        tx.setType(TransactionType.PAYMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("Paiement libre en pharmacie"+
                "\n Expediteur: " + patientWallet.get().getUser().getUsername()+
                ",\n Bénéficiaire: " + pharmacistWallet.get().getUser().getUsername());
        tx.setTimestamp(LocalDateTime.now());

        transactionRepository.save(tx);

        // 6. Notification Temps Réel (Optionnel via WebSocket)
        processTransaction(tx); // Déclenche le WebSocket
        // notificationService.sendToUser(pharmacistId, "Paiement reçu : " + amount + " FCFA");
        notificationService.notifyUser(pharmacistWallet.get().getUser().getUsername(),
                "Nouveau paiement reçu de "+ amount+ "XAF", "PAYMENT");
        return tx;
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

    public void markAsUsed(String codeValue) {
        int updatedRows = qrCodeRepository.updateIsUsedStatus(codeValue, true);

        if (updatedRows == 0) {
            throw new RuntimeException("Échec de la mise à jour : QR Code introuvable.");
        }
    }
}

