package com.frauddetection.auditor.service;

import com.frauddetection.auditor.entity.DecisionLog;
import com.frauddetection.auditor.entity.TransactionLog;
import com.frauddetection.auditor.repository.DecisionLogRepository;
import com.frauddetection.auditor.repository.TransactionLogRepository;
import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.Action;
import com.frauddetection.common.model.InvestigationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final DecisionLogRepository decisionLogRepository;
    private final TransactionLogRepository transactionLogRepository;

    @Transactional
    public DecisionLog createDecisionLog(DecisionEvent event) {
        DecisionLog log = DecisionLog.builder()
            .eventId(event.getEventId())
            .transactionId(event.getTransactionId())
            .occurredAt(event.getOccurredAt())
            .cardLast4(event.getCardLast4())
            .action(event.getAction())
            .score(event.getScore() == null ? null : BigDecimal.valueOf(event.getScore()))
            .reason(event.getReason())
            .expiresAt(event.getExpiresAt())
            .correlationId(event.getCorrelationId())
            .investigationStatus(event.getAction() == Action.BLOCK ? InvestigationStatus.CONFIRMED_FRAUD : InvestigationStatus.PENDING)
            .build();
        return decisionLogRepository.save(log);
    }

    @Transactional
    public TransactionLog createTransactionLog(TransactionEvent event) {
        TransactionLog log = TransactionLog.builder()
            .id(event.getEventId())
            .eventId(event.getEventId())
            .transactionTime(event.getOccurredAt())
            .cardLast4(event.getCardLast4())
            .cardBin(event.getCardBin())
            .amount(event.getAmount())
            .currency(event.getCurrency())
            .merchantId(event.getMerchantId())
            .merchantMcc(event.getMerchantMcc())
            .ipAddress(event.getIpAddress())
            .deviceId(event.getDeviceId())
            .build();
        return transactionLogRepository.save(log);
    }

    public List<Object[]> getDecisionStats() {
        return decisionLogRepository.countByAction();
    }

    public List<DecisionLog> getPendingInvestigations() {
        return decisionLogRepository.findByInvestigationStatus(InvestigationStatus.PENDING);
    }

    @Transactional
    public DecisionLog updateInvestigationStatus(UUID eventId, InvestigationStatus status, String notes) {
        DecisionLog log = decisionLogRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Decision not found: " + eventId));
        log.setInvestigationStatus(status);
        log.setInvestigatorNotes(notes);
        return decisionLogRepository.save(log);
    }
}
