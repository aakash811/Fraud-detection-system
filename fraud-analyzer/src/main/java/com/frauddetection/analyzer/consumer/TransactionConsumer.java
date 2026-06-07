package com.frauddetection.analyzer.consumer;

import com.frauddetection.analyzer.engine.DetectionEngine;
import com.frauddetection.analyzer.producer.DecisionProducer;
import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {

    private final DetectionEngine detectionEngine;
    private final DecisionProducer decisionProducer;

    @KafkaListener(topics = "transactions.v1", groupId = "fraud-analyzer")
    public void processTransaction(TransactionEvent event) {
        log.info("Processing transaction: {} (correlation={})", 
            event.getTransactionId(), event.getCorrelationId());
        
        DecisionEvent decision = detectionEngine.evaluate(event);
        decisionProducer.sendDecision(decision);
        
        log.info("Decision for {}: {} (score={})", 
            event.getCardLast4(), decision.getAction(), decision.getScore());
    }
}