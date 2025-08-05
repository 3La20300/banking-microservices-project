package com.banking.transactionservice.repository;

import com.banking.transactionservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByTimestampDesc(UUID fromAccountId, UUID toAccountId);

    List<Transaction> findByFromAccountIdOrderByTimestampDesc(UUID fromAccountId);

    List<Transaction> findByToAccountIdOrderByTimestampDesc(UUID toAccountId);

    List<Transaction> findByFromAccountIdOrToAccountId(UUID fromAccountId, UUID toAccountId);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);
}
