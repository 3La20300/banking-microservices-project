package com.banking.transactionservice.service;

import com.banking.transactionservice.dto.AccountDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

@Service
public class ExternalServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceClient.class);
    private final WebClient userServiceWebClient;
    private final WebClient accountServiceWebClient;
    private final MappingsEndpoint mappingsEndpoint;

    public ExternalServiceClient(@Qualifier("userServiceWebClient") WebClient userServiceWebClient,
                                 @Qualifier("accountServiceWebClient") WebClient accountServiceWebClient,
                                 MappingsEndpoint mappingsEndpoint)
    {
        this.userServiceWebClient = userServiceWebClient;
        this.accountServiceWebClient = accountServiceWebClient;
        this.mappingsEndpoint = mappingsEndpoint;
    }

    public Mono<AccountDto> getAccountById(UUID accountId)
    {
        logger.debug("Getting account with id: {}", accountId);

        return accountServiceWebClient
                .get()
                .uri("/accounts/{accountId}", accountId)
                .retrieve()
                .bodyToMono(AccountDto.class)
                .doOnSuccess(account -> logger.debug("Fetched account: {}", accountId))
                .doOnError(error -> logger.error("Error fetching account with id: {}", accountId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

//    public Mono<AccountDto> updateAccountBalance(UUID accountId, BigDecimal amount)
//    {
//        logger.debug("Updating account balance for account: {} with amount: {}", accountId, amount);
//        return accountServiceWebClient
//                .put()
//                .uri("/accounts/{accountId}/balance", accountId)
//                .bodyValue(new HashMap<String, BigDecimal>() {{
//                    put("amount", amount);
//                }})
//                .retrieve()
//                .bodyToMono(AccountDto.class)
//                .doOnSuccess(account-> logger.debug("Successfully updated account balance for account: {}", accountId))
//                .doOnError(error -> logger.error("Error updating account balance for account: {}", accountId, error))
//                .onErrorMap(WebClientResponseException.class, this:: mapWebClientException);
//    }

    // Add a method to call Account Service's transfer endpoint
    public Mono<Void> transferBetweenAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        logger.debug("Transferring amount: {} from account: {} to account: {}", amount, fromAccountId, toAccountId);

        return accountServiceWebClient
                .put()
                .uri("/accounts/transfer")
                .bodyValue(new HashMap<String, Object>() {{
                    put("fromAccountId", fromAccountId);
                    put("toAccountId", toAccountId);
                    put("amount", amount);
                }})
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> logger.debug("Successfully transferred amount from {} to {}", fromAccountId, toAccountId))
                .doOnError(error -> logger.error("Error transferring amount from {} to {}: {}", fromAccountId, toAccountId, error))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    private RuntimeException mapWebClientException(WebClientResponseException ex)
    {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getMessage();
        String responseBody = ex.getResponseBodyAsString();

        logger.error("WebClientResponseException: status={}, message={}, responseBody={}", status, message, responseBody);

        switch(status)
        {
            case NOT_FOUND :
                return new RuntimeException("Account not found");
            case BAD_REQUEST :
                return new RuntimeException("Invalid request to account service: " + message);
            case UNAUTHORIZED :
                return new RuntimeException("Unauthorized access to account service: " + message);
            case FORBIDDEN :
                return new RuntimeException("Forbidden access to account service: " + message);

            case INTERNAL_SERVER_ERROR :
                return new RuntimeException("Internal server error in account service: " + message);
            default:
                return new RuntimeException("Unexpected error from account service: " + message);
        }

    }

}
