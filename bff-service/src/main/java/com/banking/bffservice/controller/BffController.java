// BffController.java
package com.banking.bffservice.controller;

import com.banking.bffservice.dto.request.TransferExecutionRequest;
import com.banking.bffservice.dto.request.TransferInitiationRequest;
import com.banking.bffservice.dto.response.DashboardResponse;
import com.banking.bffservice.dto.response.TransferResponse;
import com.banking.bffservice.service.BffService;
import com.banking.bffservice.service.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@Validated
public class BffController {

    private static final Logger logger = LoggerFactory.getLogger(BffController.class);

    private final BffService bffService;
    private final LoggingService loggingService;

    public BffController(BffService bffService, LoggingService loggingService) {
        this.bffService = bffService;
        this.loggingService = loggingService;
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<DashboardResponse>> getDashboard(
            @PathVariable @NotBlank(message = "User ID is required") UUID userId,
            HttpServletRequest request) {

        logger.info("Received dashboard request for userId: {}", userId);

        // Log the request
        loggingService.logRequest(userId, request.getRequestURI());

        return bffService.getDashboard(userId)
                .map(dashboard -> {
                    // Log the response
                    loggingService.logResponse(dashboard, request.getRequestURI());
                    return ResponseEntity.ok(dashboard);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .doOnSuccess(response -> logger.info("Dashboard request completed for userId: {}", userId))
                .doOnError(error -> logger.error("Dashboard request failed for userId: {}", userId, error));
    }

    @PostMapping("/transactions/transfer/initiation")
    public Mono<ResponseEntity<TransferResponse>> initiateTransfer(
            @Valid @RequestBody TransferInitiationRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Received transfer initiation request: from {} to {} amount {}",
                request.getFrom_accountId(), request.getTo_accountId(), request.getAmount());

        // Log the request
        loggingService.logRequest(request, httpRequest.getRequestURI());

        return bffService.initiateTransfer(request)
                .map(response -> {
                    // Log the response
                    loggingService.logResponse(response, httpRequest.getRequestURI());
                    return ResponseEntity.ok(response);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())
                .doOnSuccess(response -> logger.info("Transfer initiation completed with transactionId: {}",
                        response.getBody() != null ? response.getBody().getTransactionId() : "unknown"))
                .doOnError(error -> logger.error("Transfer initiation failed", error));
    }

    @PostMapping("/transactions/transfer/execution")
    public Mono<ResponseEntity<TransferResponse>> executeTransfer(
            @Valid @RequestBody TransferExecutionRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Received transfer execution request for transactionId: {}", request.getTransactionId());

        // Log the request
        loggingService.logRequest(request, httpRequest.getRequestURI());

        return bffService.executeTransfer(request)
                .map(response -> {
                    // Log the response
                    loggingService.logResponse(response, httpRequest.getRequestURI());
                    return ResponseEntity.ok(response);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())
                .doOnSuccess(response -> logger.info("Transfer execution completed for transactionId: {}",
                        request.getTransactionId()))
                .doOnError(error -> logger.error("Transfer execution failed for transactionId: {}",
                        request.getTransactionId(), error));
    }
}