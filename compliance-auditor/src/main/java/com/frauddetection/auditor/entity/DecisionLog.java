package com.frauddetection.auditor.entity;

import com.frauddetection.common.model.Action;
import com.frauddetection.common.model.InvestigationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "decision_logs", indexes = {
    @Index(name = "idx_decision_card", columnList = "card_last4"),
    @Index(name = "idx_decision_time", columnList = "occurred_at"),
    @Index(name = "idx_decision_investigation", columnList = "investigation_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionLog {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "card_last4")
    private String cardLast4;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private Action action;

    @Column(name = "score", precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "correlation_id")
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "investigation_status")
    private InvestigationStatus investigationStatus;

    @Column(name = "investigator_notes", length = 2000)
    private String investigatorNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
