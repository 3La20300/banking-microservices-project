// ExternalServiceClient.java
package com.banking.bffservice.service;

import com.banking.bffservice.dto.request.TransferExecutionRequest;
import com.banking.bffservice.dto.request.TransferInitiationRequest;
import com.banking.bffservice.dto.response.Account;
import com.banking.bffservice.dto.response.Transaction;
import com.banking.bffservice.dto.response.TransferResponse;
import com.banking.bffservice.dto.response.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class ExternalServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceClient.class);

    private final WebClient userServiceWebClient;
    private final WebClient accountServiceWebClient;
    private final WebClient transactionServiceWebClient;

    public ExternalServiceClient(@Qualifier("userServiceWebClient") WebClient userServiceWebClient,
                                 @Qualifier("accountServiceWebClient") WebClient accountServiceWebClient,
                                 @Qualifier("transactionServiceWebClient") WebClient transactionServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
        this.accountServiceWebClient = accountServiceWebClient;
        this.transactionServiceWebClient = transactionServiceWebClient;
    }


    // User Service Calls
    public Mono<UserProfile> getUserProfile(UUID userId) {
        logger.debug("Fetching user profile for userId: {}", userId);

        return userServiceWebClient
                .get()
                .uri("/users/{userId}/profile", userId)
                .retrieve()
                .bodyToMono(UserProfile.class)
                .doOnSuccess(profile -> logger.debug("Successfully fetched user profile for userId: {}", userId))
                .doOnError(error -> logger.error("Error fetching user profile for userId: {}", userId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    // Account Service Calls

    //Create a new account and validate user existence in Users Database
//    public Mono<Account> createAccount(Account account) {
//        return userServiceWebClient
//                .get()
//                .uri("/users/{userId}/profile", account.getUserId())
//                .retrieve()
//                .bodyToMono(UserProfile.class)
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID does not exist")))
//                .flatMap(userProfile ->
//                        accountServiceWebClient
//                                .post()
//                                .uri("/accounts")
//                                .bodyValue(account)
//                                .retrieve()
//                                .bodyToMono(Account.class)
//                );
//    }

    //Get all accounts for a user
    public Mono<List<Account>> getUserAccounts(UUID userId) {
        logger.debug("Fetching accounts for userId: {}", userId);

        return accountServiceWebClient
                .get()
                .uri("/users/{userId}/accounts", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Account>>() {})
                .doOnSuccess(accounts -> logger.debug("Successfully fetched {} accounts for userId: {}",
                        accounts.size(), userId))
                .doOnError(error -> logger.error("Error fetching accounts for userId: {}", userId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    // Account Service Calls for Transaction Processing

    /**
     * Get a specific account by ID to check balance
     */
    public Mono<Account> getAccountById(UUID accountId) {
        logger.debug("Fetching account details for accountId: {}", accountId);

        return accountServiceWebClient
                .get()
                .uri("/accounts/{accountId}", accountId)
                .retrieve()
                .bodyToMono(Account.class)
                .doOnSuccess(account -> logger.debug("Successfully fetched account details for accountId: {}", accountId))
                .doOnError(error -> logger.error("Error fetching account details for accountId: {}", accountId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    /**
     * Check if an account has sufficient funds for a transfer
     */
    public Mono<Boolean> hasSufficientFunds(UUID accountId, BigDecimal amount) {
        logger.debug("Checking if account {} has sufficient funds for amount {}", accountId, amount);

        return getAccountById(accountId)
                .map(account -> account.getBalance().compareTo(amount) >= 0)
                .doOnSuccess(sufficient -> logger.debug("Account {} has {} funds for amount {}",
                        accountId, sufficient ? "sufficient" : "insufficient", amount))
                .doOnError(error -> logger.error("Error checking funds for accountId: {}", accountId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    /**
     * Update account balance after a transfer
     */
    public Mono<Account> updateAccountBalance(UUID accountId, BigDecimal amount) {
        logger.debug("Updating balance for account {} by amount {}", accountId, amount);

        // In a real implementation, this should use a specific API endpoint
        // to update the balance atomically, but for simplicity we'll simulate it
        return accountServiceWebClient
                .put()
                .uri("/accounts/{accountId}/balance", accountId)
                .bodyValue(new HashMap<String, BigDecimal>() {{
                    put("amount", amount);
                }})
                .retrieve()
                .bodyToMono(Account.class)
                .doOnSuccess(account -> logger.debug("Successfully updated balance for accountId: {}", accountId))
                .doOnError(error -> logger.error("Error updating balance for accountId: {}", accountId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    // Transaction Service Calls
    public Mono<List<Transaction>> getAccountTransactions(UUID accountId) {
        logger.debug("Fetching transactions for accountId: {}", accountId);

        return transactionServiceWebClient
                .get()
                .uri("/accounts/{accountId}/transactions", accountId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Transaction>>() {})
                .doOnSuccess(transactions -> logger.debug("Successfully fetched {} transactions for accountId: {}",
                        transactions.size(), accountId))
                .doOnError(error -> logger.error("Error fetching transactions for accountId: {}", accountId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    public Mono<TransferResponse> initiateTransfer(TransferInitiationRequest request) {
        logger.debug("Initiating transfer from {} to {} for amount {}",
                request.getFrom_accountId(), request.getTo_accountId(), request.getAmount());

        return transactionServiceWebClient
                .post()
                .uri("/transactions/transfer/initiation")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TransferResponse.class)
                .doOnSuccess(response -> logger.debug("Successfully initiated transfer with transactionId: {}",
                        response.getTransactionId()))
                .doOnError(error -> logger.error("Error initiating transfer", error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    public Mono<TransferResponse> executeTransfer(TransferExecutionRequest request) {
        logger.debug("Executing transfer with transactionId: {}", request.getTransactionId());

        return transactionServiceWebClient
                .post()
                .uri("/transactions/transfer/execution")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TransferResponse.class)
                .doOnSuccess(response -> logger.debug("Successfully executed transfer with transactionId: {}",
                        response.getTransactionId()))
                .doOnError(error -> logger.error("Error executing transfer with transactionId: {}",
                        request.getTransactionId(), error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    private RuntimeException mapWebClientException(WebClientResponseException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getMessage();

        logger.error("WebClient error - Status: {}, Message: {}", status, message);

        switch (status) {
            case NOT_FOUND:
                return new RuntimeException("Resource not found: " + message);
            case BAD_REQUEST:
                return new RuntimeException("Invalid request: " + message);
            case UNAUTHORIZED:
                return new RuntimeException("Unauthorized access: " + message);
            case INTERNAL_SERVER_ERROR:
                return new RuntimeException("Internal server error: " + message);
            default:
                return new RuntimeException("External service error: " + message);
        }
    }
}