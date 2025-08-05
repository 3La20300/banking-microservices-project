package com.banking.transactionservice.service;

import com.banking.transactionservice.dto.*;
import com.banking.transactionservice.exception.InvalidAccountException;
import com.banking.transactionservice.exception.InvalidTransactionException;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TransactionService.class);

    private final ExternalServiceClient externalServiceClient;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(ExternalServiceClient externalServiceClient, TransactionRepository transactionRepository) {
        this.externalServiceClient = externalServiceClient;
        this.transactionRepository = transactionRepository;
    }


    // This would be typically injected, but for now we'll leave a comment
    // @Autowired
    // private AccountServiceClient accountServiceClient;


    //    @Transactional : Not used with Reactive progeramming, but kept for clarity
    public Mono<TransactionResponseDto> initiateTransaction(TransactionInitiationDto dto) {
        UUID fromId = dto.getFromAccountId();
        UUID toId = dto.getToAccountId();

        if (fromId == null || toId == null) {
            return Mono.error(new InvalidAccountException("Invalid account IDs"));
        }

        if (fromId.equals(toId)) {
            return Mono.error(new InvalidTransactionException("Source and destination accounts cannot be the same"));
        }

        Mono<AccountDto> fromAccountMono = externalServiceClient.getAccountById(fromId);
        Mono<AccountDto> toAccountMono = externalServiceClient.getAccountById(toId);

        return Mono.zip(fromAccountMono, toAccountMono)
                .flatMap(tuple -> {
                    AccountDto from = tuple.getT1();
                    AccountDto to = tuple.getT2();

                    if (!"ACTIVE".equals(from.getStatus()) || !"ACTIVE".equals(to.getStatus())) {
                        return Mono.error(new InvalidAccountException("One of the accounts is not active"));
                    }

                    if (from.getBalance().compareTo(dto.getAmount()) < 0) {
                        return Mono.error(new InvalidTransactionException("Insufficient funds"));
                    }

                    Transaction transaction = new Transaction();
                    transaction.setFromAccountId(fromId);
                    transaction.setToAccountId(toId);
                    transaction.setAmount(dto.getAmount());
                    transaction.setDescription(dto.getDescription());
                    transaction.setStatus(Transaction.TransactionStatus.INITIATED);

                    return Mono.fromCallable(() -> transactionRepository.save(transaction))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(saved -> new TransactionResponseDto(
                                    saved.getTransactionId(),
                                    "Initiated",
                                    LocalDateTime.now()
                            ));
                });
    }

//Why many returns ? Think of it like:
//
//You ask the bank for account info → Mono<AccountDto>
//
//When the info comes back, you create a transaction → Mono<TransactionResponseDto>
//
//The user only receives the final result (transaction response), not the intermediate account.
//MONO means "I will give you a single value in the future". used with Reactive programming to handle asynchronous operations.
//    @Transactional : Not used with Reactive progeramming, but kept for clarity
public Mono<TransactionResponseDto> executeTransaction(UUID transactionId) {
//    UUID transactionId = transactionExecutionDto.getTransactionId(); // Assuming this comes from the DTO or request body
    if (transactionId == null) {
        return Mono.error(new InvalidTransactionException("Invalid transaction ID"));
    }

    return Mono.fromCallable(() -> transactionRepository.findById(transactionId)
                .orElseThrow(() -> new InvalidTransactionException("Transaction not found")))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(transaction -> {
            UUID fromAccountId = transaction.getFromAccountId();
            UUID toAccountId = transaction.getToAccountId();

            if (transaction.getStatus() != Transaction.TransactionStatus.INITIATED) {
                return Mono.error(new InvalidTransactionException("Transaction already executed or invalid state"));
            }

            Mono<AccountDto> fromAccountMono = externalServiceClient.getAccountById(fromAccountId);
            Mono<AccountDto> toAccountMono = externalServiceClient.getAccountById(toAccountId);

            return Mono.zip(fromAccountMono, toAccountMono)
                    .flatMap(tuple -> {
                        AccountDto from = tuple.getT1();
                        AccountDto to = tuple.getT2();

                        if (!"ACTIVE".equals(from.getStatus()) || !"ACTIVE".equals(to.getStatus())) {
                            transaction.setStatus(Transaction.TransactionStatus.FAILED);
                            return Mono.fromCallable(() -> transactionRepository.save(transaction))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .then(Mono.error(new InvalidAccountException("One of the accounts is not active")));
                        }

                        if (from.getBalance().compareTo(transaction.getAmount()) < 0) {
                            transaction.setStatus(Transaction.TransactionStatus.FAILED);
                            return Mono.fromCallable(() -> transactionRepository.save(transaction))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .then(Mono.error(new InvalidTransactionException("Insufficient funds in source account")));
                        }

                        // Use the transferBetweenAccounts method instead of two separate calls
                        return externalServiceClient.transferBetweenAccounts(fromAccountId, toAccountId, transaction.getAmount())
                            .then(Mono.fromCallable(() -> {
                                transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
                                return transactionRepository.save(transaction);
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(saved -> new TransactionResponseDto(
                                saved.getTransactionId(),
                                "Success",
                                LocalDateTime.now()
                            )));
                    });
        });
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
            // This is optional, depends on business logic : REQUIRENENT SATISFACTION purpose
            throw new InvalidAccountException("No transactions found for this account ID");
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
                    BigDecimal amount = tx.getFromAccountId().equals(accountId)
                            ? tx.getAmount().negate() // Outgoing money (negative)
                            : tx.getAmount(); // Incoming money (positive)

                    transactionHistoryDto.setAmount(amount);
                    transactionHistoryDto.setDescription(tx.getDescription());
                    transactionHistoryDto.setTimestamp(tx.getTimestamp());

                    // Set the current balance as null or a placeholder
                    transactionHistoryDto.setCurrentBalance(runningBalance);

                    return transactionHistoryDto;
                }).collect(Collectors.toList());
    }

    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getTransactionId(),
                transaction.getStatus().toString(),
                transaction.getTimestamp()
        );
    }
}
