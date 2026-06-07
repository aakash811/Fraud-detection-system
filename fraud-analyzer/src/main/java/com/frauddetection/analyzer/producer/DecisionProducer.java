package com.frauddetection.analyzer.producer;

import com.frauddetection.common.event.DecisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RetriableException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionProducer {

    private static final String TOPIC = "decisions.v1";

    private final KafkaTemplate<String, DecisionEvent> kafkaTemplate;

    public void sendDecision(DecisionEvent decision) {
        String key = decision.getCorrelationId() != null ? decision.getCorrelationId() : decision.getCardLast4();
        ProducerRecord<String, DecisionEvent> record = 
            new ProducerRecord<>(TOPIC, key, decision);
        
        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send decision: {}", ex.getMessage());
                        if (ex.getCause() instanceof RetriableException) {
                            log.warn("Retriable error for decision");
                        }
                    }
                });
    }
}