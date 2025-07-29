// BffControllerIntegrationTest.java
package com.banking.bffservice.controller;

import com.banking.bffservice.dto.request.TransferExecutionRequest;
import com.banking.bffservice.dto.request.TransferInitiationRequest;
import com.banking.bffservice.dto.response.*;
import com.banking.bffservice.service.BffService;
import com.banking.bffservice.service.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = BffController.class)
@ActiveProfiles("test")
class BffControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BffService bffService;

    @MockBean
    private LoggingService loggingService;

    @Test
    void testGetDashboard() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Create mock response
        UserProfile userProfile = new UserProfile(
            userId,
            "testUser",
            "test@example.com",
            "Test",
            "User"
        );

        List<Account> accounts = new ArrayList<>();
        UUID accountId = UUID.randomUUID();
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
            UUID.randomUUID(),
            accountId,
            BigDecimal.valueOf(100.0),
            "Test transaction",
            LocalDateTime.now(),
            BigDecimal.valueOf(100.0)
        ));

        Account account = new Account();
        account.setAccountId(accountId);
        account.setAccountNumber("123456789");
        account.setAccountType("SAVINGS");
        account.setBalance(BigDecimal.valueOf(100.0));
        account.setStatus("ACTIVE");
        account.setTransactions(transactions);
        accounts.add(account);

        DashboardResponse mockResponse = new DashboardResponse(
            userId,
            userProfile.getUsername(),
            userProfile.getEmail(),
            userProfile.getFirstName(),
            userProfile.getLastName(),
            accounts
        );

        when(bffService.getDashboard(any(UUID.class))).thenReturn(Mono.just(mockResponse));

        // Act & Assert
        webTestClient.get()
                .uri("/dashboard/" + userId)
                .header("APP-NAME", "PORTAL")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DashboardResponse.class);
    }

    @Test
    void testInitiateTransfer() {
        // Arrange
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransferInitiationRequest request = new TransferInitiationRequest(
            fromAccountId,
            toAccountId,
            new BigDecimal("100.00"),
            "Test transfer"
        );

        TransferResponse mockResponse = new TransferResponse(
            transactionId,
            fromAccountId,
            toAccountId,
            new BigDecimal("100.00"),
            "Transfer Initiated",
            LocalDateTime.now()
        );

        when(bffService.initiateTransfer(any(TransferInitiationRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/transactions/transfer/initiation")
                .header("APP-NAME", "PORTAL")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferResponse.class);
    }

    @Test
    void testExecuteTransfer() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        TransferExecutionRequest request = new TransferExecutionRequest(
            transactionId,
            fromAccountId,
            toAccountId,
            new BigDecimal("100.00"),
            "Test transfer execution"
        );

        TransferResponse mockResponse = new TransferResponse(
            transactionId,
            fromAccountId,
            toAccountId,
            new BigDecimal("100.00"),
            "Transfer Success",
            LocalDateTime.now()
        );

        when(bffService.executeTransfer(any(TransferExecutionRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/transactions/transfer/execution")
                .header("APP-NAME", "PORTAL")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferResponse.class);
    }
}