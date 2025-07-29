// BffService.java
package com.banking.bffservice.service;

import com.banking.bffservice.dto.request.TransferExecutionRequest;
import com.banking.bffservice.dto.request.TransferInitiationRequest;
import com.banking.bffservice.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class BffService {

    private static final Logger logger = LoggerFactory.getLogger(BffService.class);

    @Autowired
    private final ExternalServiceClient externalServiceClient;

    public BffService(ExternalServiceClient externalServiceClient) {
        this.externalServiceClient = externalServiceClient;
    }

    public Mono<DashboardResponse> getDashboard(UUID userId) {
        logger.info("Building dashboard for userId: {}", userId);

        // Step 1: Get user profile
        Mono<UserProfile> userProfileMono = externalServiceClient.getUserProfile(userId);

        // Step 2: Get user accounts
        Mono<List<Account>> accountsMono = externalServiceClient.getUserAccounts(userId);

        // Step 3: Combine user profile and accounts, then fetch transactions for each account
        return Mono.zip(userProfileMono, accountsMono)
                .flatMap(tuple -> {
                    UserProfile userProfile = tuple.getT1();
                    List<Account> accounts = tuple.getT2();

                    // Step 4: For each account, fetch transactions asynchronously
                    Flux<Account> accountsWithTransactions = Flux.fromIterable(accounts)
                            .flatMap(account ->
                                    externalServiceClient.getAccountTransactions(account.getAccountId())
                                            .map(transactions -> {
                                                account.setTransactions(transactions);
                                                return account;
                                            })
                                            .onErrorReturn(account) // If transaction fetch fails, return account without transactions
                            );

                    // Step 5: Collect all accounts with transactions and build dashboard response
                    return accountsWithTransactions.collectList()
                            .map(accountsWithTx -> new DashboardResponse(
                                    userProfile.getUserId(),
                                    userProfile.getUsername(),
                                    userProfile.getEmail(),
                                    userProfile.getFirstName(),
                                    userProfile.getLastName(),
                                    accountsWithTx
                            ));
                })
                .doOnSuccess(dashboard -> logger.info("Successfully built dashboard for userId: {} with {} accounts",
                        userId, dashboard.getAccounts().size()))
                .doOnError(error -> logger.error("Error building dashboard for userId: {}", userId, error));
    }

    public Mono<TransferResponse> initiateTransfer(TransferInitiationRequest request) {
        logger.info("Initiating transfer from {} to {} for amount {}",
                request.getFrom_accountId(), request.getTo_accountId(), request.getAmount());

        // First check if both accounts exist
        Mono<Account> fromAccountMono = externalServiceClient.getAccountById(request.getFrom_accountId());
        Mono<Account> toAccountMono = externalServiceClient.getAccountById(request.getTo_accountId());

        return Mono.zip(fromAccountMono, toAccountMono)
                .flatMap(tuple -> {
                    Account fromAccount = tuple.getT1();
                    Account toAccount = tuple.getT2();

                    // Check if from account has sufficient funds
                    if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new RuntimeException("Insufficient funds in source account"));
                    }

                    // Proceed with transfer initiation
                    return externalServiceClient.initiateTransfer(request);
                })
                .doOnSuccess(response -> logger.info("Successfully initiated transfer with transactionId: {}",
                        response.getTransactionId()))
                .doOnError(error -> logger.error("Error initiating transfer", error));
    }

    public Mono<TransferResponse> executeTransfer(TransferExecutionRequest request) {
        logger.info("Executing transfer with transactionId: {}", request.getTransactionId());

        // First, check if the source account has sufficient funds
        return externalServiceClient.getAccountById(request.getFromAccountId())
                .flatMap(fromAccount -> {
                    // Validate sufficient funds
                    if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new RuntimeException("Insufficient funds in source account"));
                    }

                    // Execute the transaction first
                    return externalServiceClient.executeTransfer(request)
                            .flatMap(response -> {
                                // If successful, update account balances
                                Mono<Account> debitAccount = externalServiceClient
                                        .updateAccountBalance(request.getFromAccountId(), request.getAmount().negate());

                                Mono<Account> creditAccount = externalServiceClient
                                        .updateAccountBalance(request.getToAccountId(), request.getAmount());

                                return Mono.zip(debitAccount, creditAccount)
                                        .map(tuple -> response); // Return the original response
                            });
                })
                .doOnSuccess(response -> logger.info("Successfully executed transfer with transactionId: {}",
                        response.getTransactionId()))
                .doOnError(error -> logger.error("Error executing transfer with transactionId: {}",
                        request.getTransactionId(), error));
    }
}