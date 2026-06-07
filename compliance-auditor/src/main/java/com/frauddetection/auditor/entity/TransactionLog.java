package com.frauddetection.auditor.entity;

import com.frauddetection.common.model.Action;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_logs", indexes = {
    @Index(name = "idx_card_last4", columnList = "card_last4"),
    @Index(name = "idx_transaction_time", columnList = "transaction_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLog {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "transaction_time")
    private Instant transactionTime;

    @Column(name = "card_last4")
    private String cardLast4;

    @Column(name = "card_bin")
    private String cardBin;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "merchant_mcc")
    private String merchantMcc;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_id")
    private String deviceId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}