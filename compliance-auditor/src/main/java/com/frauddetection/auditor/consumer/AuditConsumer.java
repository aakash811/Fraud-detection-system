package com.frauddetection.auditor.consumer;

import com.frauddetection.auditor.entity.DecisionLog;
import com.frauddetection.auditor.entity.TransactionLog;
import com.frauddetection.auditor.service.AuditService;
import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditConsumer {

    private final AuditService auditService;

    @KafkaListener(
        topics = "transactions.v1",
        groupId = "compliance-auditor-transactions",
        containerFactory = "transactionKafkaListenerContainerFactory"
    )
    public void processTransaction(TransactionEvent event) {
        try {
            TransactionLog txnLog = auditService.createTransactionLog(event);
            log.info("Persisted transaction: {}", txnLog.getId());
        } catch (Exception e) {
            log.error("Failed to persist transaction: {}", e.getMessage());
        }
    }

    @KafkaListener(
        topics = "decisions.v1",
        groupId = "compliance-auditor-decisions",
        containerFactory = "decisionKafkaListenerContainerFactory"
    )
    public void processDecision(DecisionEvent event) {
        try {
            DecisionLog decLog = auditService.createDecisionLog(event);
            log.info("Persisted decision: {} - {}", decLog.getTransactionId(), decLog.getAction());
        } catch (Exception e) {
            log.error("Failed to persist decision: {}", e.getMessage());
        }
    }
}
