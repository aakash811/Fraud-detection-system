package com.frauddetection.ingestor.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
    @NotBlank String cardLast4,
    @NotBlank String cardBin,
    String userId,
    String accountId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank String currency,
    @NotBlank String merchantId,
    String merchantMcc,
    String merchantName,
    @NotBlank String ipAddress,
    String deviceId,
    Double latitude,
    Double longitude,
    String referenceId,
    String idempotencyKey
) {}