package com.medipay.repository;


import com.medipay.entity.Transaction;
import com.medipay.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderWalletOrderByTimestampDesc(Wallet senderWallet);
    List<Transaction> findByReceiverWalletOrderByTimestampDesc(Wallet receiverWallet);

    // Pour l'historique global d'un utilisateur (envoi ou réception)
    List<Transaction> findBySenderWalletOrReceiverWalletOrderByTimestampDesc(Wallet sender, Wallet receiver);
}
