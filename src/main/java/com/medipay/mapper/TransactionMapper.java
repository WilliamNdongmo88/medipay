package com.medipay.mapper;

import com.medipay.dto.TransactionResponse;
import com.medipay.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setTimestamp(transaction.getTimestamp());

        // 🔥 Sender
        if (transaction.getSenderWallet() != null &&
                transaction.getSenderWallet().getUser() != null) {
            response.setSenderName(
                    transaction.getSenderWallet().getUser().getUsername()
            );
        }

        // 🔥 Receiver
        if (transaction.getReceiverWallet() != null &&
                transaction.getReceiverWallet().getUser() != null) {
            response.setReceiverName(
                    transaction.getReceiverWallet().getUser().getUsername()
            );
        }

        return response;
    }

    public List<TransactionResponse> toResponseList(List<Transaction> transactions) {
        if (transactions == null) {
            return List.of();
        }

        return transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
