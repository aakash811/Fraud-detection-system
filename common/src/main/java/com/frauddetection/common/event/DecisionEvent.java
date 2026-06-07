package com.frauddetection.common.event;

import com.frauddetection.common.model.Action;
import com.frauddetection.common.model.RuleResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionEvent {
    private UUID eventId;
    private String schemaVersion;
    private Instant occurredAt;
    private UUID transactionId;
    private String cardLast4;
    private String correlationId;
    private Action action;
    private Double score;
    private List<RuleResult> triggeredRules;
    private String reason;
    private Instant expiresAt;
}