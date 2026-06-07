package com.frauddetection.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {
    private UUID eventId;
    private String schemaVersion;
    private Instant occurredAt;
    private String cardLast4;
    private String cardBin;
    private String userId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String merchantId;
    private String merchantMcc;
    private String merchantName;
    private String ipAddress;
    private String deviceId;
    private Double latitude;
    private Double longitude;
    private String transactionId;
    private String referenceId;
    private String correlationId;
    private String idempotencyKey;
    private Map<String, String> metadata;
}