package com.banking.transactionservice.service;

import com.banking.transactionservice.dto.TransactionExecutionDto;
import com.banking.transactionservice.dto.TransactionHistoryDto;
import com.banking.transactionservice.dto.TransactionInitiationDto;
import com.banking.transactionservice.dto.TransactionResponseDto;
import com.banking.transactionservice.exception.InsufficientFundsException;
import com.banking.transactionservice.exception.InvalidAccountException;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.exception.InvalidTransactionException;
import com.banking.transactionservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // This would be typically injected, but for now we'll leave a comment
    // @Autowired
    // private AccountServiceClient accountServiceClient;

    @Transactional
    public TransactionResponseDto initiateTransaction(TransactionInitiationDto transactionInitiationDto) {
        // Validate account IDs (in a real scenario, this would call the Account Service)
        UUID fromAccountId = transactionInitiationDto.getFromAccountId();
        UUID toAccountId = transactionInitiationDto.getToAccountId();

        // Check if account IDs are valid
        if (fromAccountId == null || toAccountId == null) {
            throw new InvalidAccountException("Invalid 'from' or 'to' account ID.");
        }

        // Check if it's not a self-transfer
        if (fromAccountId.equals(toAccountId)) {
            throw new InvalidTransactionException("Source and destination accounts cannot be the same");
        }

        Transaction transaction= new Transaction();
        transaction.setFrom_accountId(fromAccountId);
        transaction.setTo_accountId(toAccountId);
        transaction.setAmount(transactionInitiationDto.getAmount());
        transaction.setDescription("Transaction Initiated");
        transaction.setStatus(Transaction.AccountStatus.INITIATED);

        Transaction saved = transactionRepository.save(transaction);

        return mapToResponseDto(saved);
    }

    @Transactional
    public TransactionResponseDto executeTransaction(TransactionExecutionDto transactionExecutionDto) {
        Transaction transaction = transactionRepository.findById(transactionExecutionDto.getTransactionId())
                .orElseThrow(() -> new InvalidTransactionException("Transaction not found"));

        if (!transaction.getFrom_accountId().equals(transactionExecutionDto.getFromAccountId()) ||
        !transaction.getTo_accountId().equals(transactionExecutionDto.getToAccountId())) {
            throw new InvalidTransactionException("Transaction details do not match");
        }

        if (transaction.getStatus() != Transaction.AccountStatus.INITIATED) {
            throw new InvalidTransactionException("Transaction already executed or invalid state");
        }

        // In a real application with communication to the Account Service, we would:
        // 1. Check if the source account has sufficient funds
        // 2. Update both account balances

        // Simulation of checking for sufficient funds
        // boolean hasSufficientFunds = accountServiceClient.checkSufficientFunds(
        //    transaction.getFrom_accountId(), transaction.getAmount());
        // if (!hasSufficientFunds) {
        //    throw new InsufficientFundsException("Insufficient funds in source account");
        // }

        // Since in this microservices architecture, the BFF will handle the communication
        // with the Account Service, we'll just update the transaction status here
        transaction.setStatus(Transaction.AccountStatus.SUCCESS);
        transactionRepository.save(transaction);
        return mapToResponseDto(transaction);
    }

    @Transactional
    public List<TransactionHistoryDto> getTransactionHistory(UUID accountId) {
        // Validate account ID
        if (accountId == null) {
            throw new InvalidAccountException("Invalid account ID");
        }

        List<Transaction> transactions = transactionRepository.
                findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId);

        if (transactions.isEmpty()) {
            // This is optional, depends on your business logic
            // You might want to return empty list instead
            // throw new InvalidAccountException("No transactions found for this account ID");
        }

        // In a real scenario, we would get the current balance from the Account Service
        // BigDecimal currentBalance = accountServiceClient.getAccountBalance(accountId);

        // For now, we'll use a placeholder value that the BFF will replace
        BigDecimal runningBalance = BigDecimal.ZERO; // Placeholder

        return transactions.stream()
                .map(tx -> {
                    TransactionHistoryDto transactionHistoryDto = new TransactionHistoryDto();
                    transactionHistoryDto.setTransactionId(tx.getTransactionId());
                    transactionHistoryDto.setAccountId(accountId);

                    // Determine if this is a debit or credit for this account
                    BigDecimal amount = tx.getFrom_accountId().equals(accountId)
                            ? tx.getAmount().negate() // Outgoing money (negative)
                            : tx.getAmount(); // Incoming money (positive)

                    transactionHistoryDto.setAmount(amount);
                    transactionHistoryDto.setDescription(tx.getDescription());
                    transactionHistoryDto.setTimestamp(tx.getTimestamp());

                    // Set the current balance as null or a placeholder
                    // The actual value will be calculated by the BFF when aggregating data
                    transactionHistoryDto.setCurrentBalance(runningBalance);

                    return transactionHistoryDto;
                }).collect(Collectors.toList());
    }

    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getTransactionId(),
                transaction.getFrom_accountId(),
                transaction.getTo_accountId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTimestamp()
        );
    }
}
