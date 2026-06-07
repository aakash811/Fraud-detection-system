package com.frauddetection.alert.producer;

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
public class AlertProducer {

    private static final String TOPIC = "fraud-alerts.v1";

    private final KafkaTemplate<String, DecisionEvent> kafkaTemplate;

    public void sendAlert(DecisionEvent event) {
        ProducerRecord<String, DecisionEvent> record = 
            new ProducerRecord<>(TOPIC, event.getCardLast4(), event);
        
        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send fraud alert: {}", ex.getMessage());
                        if (ex.getCause() instanceof RetriableException) {
                            log.warn("Retriable error for fraud alert");
                        }
                    } else {
                        log.debug("Fraud alert sent to partition {} offset {}", 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });
    }
}