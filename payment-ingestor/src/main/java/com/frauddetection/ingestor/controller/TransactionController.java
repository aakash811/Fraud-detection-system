package com.frauddetection.ingestor.controller;

import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.ingestor.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transactions")
    @Operation(summary = "Submit a payment transaction for fraud analysis")
    @ApiResponse(responseCode = "202", description = "Transaction accepted for processing")
    public ResponseEntity<Void> submitTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionEvent event = TransactionEvent.builder()
                .eventId(UUID.randomUUID())
                .schemaVersion("v1")
                .occurredAt(Instant.now())
                .cardLast4(request.cardLast4())
                .cardBin(request.cardBin())
                .userId(request.userId())
                .accountId(request.accountId())
                .amount(request.amount())
                .currency(request.currency())
                .merchantId(request.merchantId())
                .merchantMcc(request.merchantMcc())
                .merchantName(request.merchantName())
                .ipAddress(request.ipAddress())
                .deviceId(request.deviceId())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .transactionId(UUID.randomUUID().toString())
                .referenceId(request.referenceId())
                .correlationId(UUID.randomUUID().toString())
                .idempotencyKey(request.idempotencyKey())
                .build();
        
        transactionService.publishTransaction(event);
        return ResponseEntity.accepted().build();
    }
}