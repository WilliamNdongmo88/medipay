package com.medipay.repository;


import com.medipay.entity.Transaction;
import com.medipay.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderWalletOrderByTimestampDesc(Wallet senderWallet);
    List<Transaction> findByReceiverWalletOrderByTimestampDesc(Wallet receiverWallet);


    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.senderWallet sw
        LEFT JOIN FETCH sw.user
        LEFT JOIN FETCH t.receiverWallet rw
        LEFT JOIN FETCH rw.user
        WHERE t.senderWallet = :wallet OR t.receiverWallet = :wallet
        ORDER BY t.timestamp DESC
    """)
    List<Transaction> findAllWithUsers(@Param("wallet") Wallet wallet);
}
