package com.frauddetection.orchestrator.service;

import com.frauddetection.orchestrator.model.RuleConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RedisTemplate<String, Object> redisTemplate;

    public DecisionResponse getDecision(String cardLast4, String ipAddress) {
        if (cardLast4 != null) {
            Map<Object, Object> decision = redisTemplate.opsForHash().entries("decision:" + cardLast4);
            if (!decision.isEmpty()) {
                return new DecisionResponse(
                    (String) decision.get("action"),
                    decision.get("score") != null ? ((Number) decision.get("score")).doubleValue() : 0.0,
                    (String) decision.get("reason"),
                    Instant.parse((String) decision.get("expiresAt")),
                    (String) decision.get("correlationId")
                );
            }
        }
        return new DecisionResponse("ALLOW", 0.0, "No active decision", Instant.now().plusSeconds(60), null);
    }

    public RuleConfiguration getCurrentConfiguration() {
        RuleConfiguration config = new RuleConfiguration();
        config.setWeights(Map.of(
            "velocity", 0.25,
            "blacklist", 0.30,
            "geolocation", 0.20,
            "time-based", 0.15,
            "amount-anomaly", 0.20,
            "device-fingerprint", 0.10,
            "merchant-pattern", 0.10,
            "ip-velocity", 0.15
        ));
        return config;
    }

    public void updateWeights(Map<String, Double> weights) {
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            redisTemplate.opsForValue().set("rule:weight:" + entry.getKey(), entry.getValue());
        }
    }

    public record DecisionResponse(String action, double score, String reason, Instant expiresAt, String correlationId) {
    }
}
