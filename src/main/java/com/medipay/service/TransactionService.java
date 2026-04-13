package com.medipay.service;

import com.medipay.dto.TransactionResponse;
import com.medipay.entity.Transaction;
import com.medipay.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public List<TransactionResponse> getAllTransactionsForAdmin() {
        // Récupérer toutes les transactions triées par date décroissante
        List<Transaction> transactions = transactionRepository.findAllByOrderByTimestampDesc();

        return transactions.stream().map(tx -> {
            TransactionResponse dto = new TransactionResponse();
            dto.setId(tx.getId());
            dto.setReceiverName(tx.getReceiverWallet().getUser().getUsername());
            dto.setSenderBalance(tx.getSenderWallet() != null ? tx.getSenderWallet().getBalance() : null);
            dto.setReceiverBalance(tx.getReceiverWallet() != null ? tx.getReceiverWallet().getBalance() : null);
            dto.setAmount(tx.getAmount());
            dto.setType(tx.getType());
            dto.setStatus(tx.getStatus());
            dto.setTimestamp(tx.getTimestamp());
            dto.setDescription(tx.getDescription());
            return dto;
        }).collect(Collectors.toList());
    }
}

