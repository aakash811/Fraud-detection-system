package com.frauddetection.ingestor.producer;

import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RetriableException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private static final String TOPIC = "transactions.v1";
    private static final String IDEMPOTENCY_PREFIX = "idempotency:txn:";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public void sendTransaction(TransactionEvent event) {
        if (event.getIdempotencyKey() != null) {
            String idemKey = IDEMPOTENCY_PREFIX + event.getIdempotencyKey();
            Boolean alreadyProcessed = redisTemplate.hasKey(idemKey);
            if (Boolean.TRUE.equals(alreadyProcessed)) {
                log.debug("Transaction already processed, skipping: {}", event.getIdempotencyKey());
                return;
            }
            redisTemplate.opsForValue().set(idemKey, "processed", Duration.ofHours(24));
        }
        
        ProducerRecord<String, TransactionEvent> record = new ProducerRecord<>(TOPIC, event.getCardLast4(), event);
        
        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send transaction: {}", ex.getMessage());
                        if (ex.getCause() instanceof RetriableException) {
                            log.warn("Retriable error occurred, will retry");
                        }
                    } else {
                        log.debug("Transaction sent to partition {} offset {}", 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });
    }
}