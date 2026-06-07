package com.frauddetection.orchestrator.consumer;

import com.frauddetection.common.event.DecisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "decisions.v1", groupId = "risk-orchestrator")
    public void processDecision(DecisionEvent event) {
        String key = "decision:" + event.getCardLast4();
        redisTemplate.opsForHash().putAll(key, Map.of(
            "action", event.getAction().name(),
            "score", event.getScore(),
            "reason", event.getReason() == null ? "" : event.getReason(),
            "expiresAt", event.getExpiresAt().toString(),
            "correlationId", event.getCorrelationId(),
            "cardLast4", event.getCardLast4()
        ));

        Duration ttl = Duration.between(Instant.now(), event.getExpiresAt());
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.expire(key, ttl);
        }

        log.info("Cached decision for {}: {} (score={}, correlation={})", 
            event.getCardLast4(), event.getAction(), event.getScore(), event.getCorrelationId());
    }
}
