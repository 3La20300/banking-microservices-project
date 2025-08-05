// BffController.java
package com.banking.bffservice.controller;

import com.banking.bffservice.dto.response.DashboardResponse;
import com.banking.bffservice.service.BffService;
import com.banking.bffservice.service.KafkaLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/")
@Validated
public class BffController {

    private static final Logger logger = LoggerFactory.getLogger(BffController.class);

    @Autowired
    private final BffService bffService;

    @Autowired
    private final KafkaLoggingService kafkaLoggingService;

    public BffController(BffService bffService, KafkaLoggingService kafkaLoggingService) {
        this.bffService = bffService;
        this.kafkaLoggingService = kafkaLoggingService;
    }

    @GetMapping("/dashboard/{userId}")
    public Mono<ResponseEntity<DashboardResponse>> getDashboard(
            @PathVariable @NotNull(message = "User ID is required") UUID userId,
            HttpServletRequest request) {

        logger.info("Received dashboard request for userId: {}", userId);

        // Log the request
//        kafkaLoggingService.logRequest(userId, request.getRequestURI());

        return bffService.getDashboard(userId)
                .map(dashboard -> {
                    // Log the response
//                    kafkaLoggingService.logResponse(dashboard, request.getRequestURI());
                    return ResponseEntity.ok(dashboard);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .doOnSuccess(response -> logger.info("Dashboard request completed for userId: {}", userId))
                .doOnError(error -> logger.error("Dashboard request failed for userId: {}", userId, error));
    }

//    @PostMapping("/transactions/transfer/initiation")
//    public Mono<ResponseEntity<Object>> initiateTransfer(
//            @Valid @RequestBody TransferInitiationRequest request,
//            HttpServletRequest httpRequest) {
//
//        logger.info("Received transfer initiation request: from {} to {} amount {}",
//                request.getFromAccountId(), request.getToAccountId(), request.getAmount());
//
//        // Log the request
////        kafkaLoggingService.logRequest(request, httpRequest.getRequestURI());
//
//        return bffService.initiateTransfer(request)
//                .map(response -> {
//                    // Log the response
////                    kafkaLoggingService.logResponse(response, httpRequest.getRequestURI());
//                    return ResponseEntity.ok((Object)response);
//                })
//                .onErrorResume(error -> {
//                    // Log the error
//                    logger.error("Transfer initiation failed", error);
//
//                    // Create error response object
//                    ErrorResponseDto errorResponse = new ErrorResponseDto(
//                        HttpStatus.BAD_REQUEST.value(),
//                        "BAD REQUEST",
//                        "Error: " + error.getMessage()
//                    );
//
//                    // Log the error response
////                    kafkaLoggingService.logResponse(errorResponse, httpRequest.getRequestURI());
//
//                    // Return error response directly instead of throwing exception
//                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
//                })
//                .doOnSuccess(response -> logger.info("Transfer initiation completed"));
//    }
//
//    @PostMapping("/transactions/transfer/execution")
//    public Mono<ResponseEntity<Object>> executeTransfer(
//            @Valid @RequestBody TransferExecutionRequest request,
//            HttpServletRequest httpRequest) {
//
//        logger.info("Received transfer execution request for transactionId: {}", request.getTransactionId());
//
//        // Log the request
////        kafkaLoggingService.logRequest(request, httpRequest.getRequestURI());
//
//        return bffService.executeTransfer(request)
//                .map(response -> {
//                    // Log the response
////                    kafkaLoggingService.logResponse(response, httpRequest.getRequestURI());
//                    return ResponseEntity.ok((Object)response);
//                })
//                .onErrorResume(error -> {
//                    logger.error("Transfer execution failed for transactionId: {}, Error: {}",
//                            request.getTransactionId(), error.getMessage());
//
//                    // Create error response object
//                    ErrorResponseDto errorResponse = new ErrorResponseDto(
//                        HttpStatus.BAD_REQUEST.value(),
//                        "BAD REQUEST",
//                        "Error: " + error.getMessage()
//                    );
//
//                    // Log the error response
////                    kafkaLoggingService.logResponse(errorResponse, httpRequest.getRequestURI());
//
//                    // Return error response directly instead of throwing exception
//                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
//                })
//                .doOnSuccess(response -> logger.info("Transfer execution completed for transactionId: {}",
//                        request.getTransactionId()))
//                .doOnError(error -> logger.error("Transfer execution failed for transactionId: {}",
//                        request.getTransactionId(), error));
//    }
}