package com.frauddetection.analyzer.engine;

import com.frauddetection.common.model.RuleResult;
import com.frauddetection.analyzer.rules.DetectionRule;
import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class DetectionEngine {

    private final List<DetectionRule> rules;
    private static final double OVERRIDE_THRESHOLD = 0.9;

    public DetectionEngine(List<DetectionRule> rules) {
        this.rules = rules;
    }

    public DecisionEvent evaluate(TransactionEvent event) {
        List<RuleResult> results = new ArrayList<>();
        double totalScore = 0.0;
        double totalWeight = 0.0;
        boolean overrideTriggered = false;
        String overrideRule = null;

        for (DetectionRule rule : rules) {
            try {
                RuleResult result = rule.evaluate(event);
                results.add(result);
                totalScore += result.score() * result.weight();
                if (result.score() > 0) {
                    totalWeight += result.weight();
                }
                if (result.score() >= OVERRIDE_THRESHOLD) {
                    overrideTriggered = true;
                    overrideRule = rule.getName();
                    log.warn("High-risk override triggered by rule: {} for card {}", rule.getName(), event.getCardLast4());
                }
            } catch (Exception e) {
                log.error("Rule {} failed", rule.getName(), e);
                results.add(RuleResult.of(rule.getName(), 0.0, rule.getWeight(), "Rule failed"));
            }
        }

        double normalizedScore = totalWeight > 0 ? totalScore / totalWeight : 0.0;
        Action action = overrideTriggered ? Action.BLOCK : Action.fromScore(normalizedScore);
        String reason = overrideTriggered 
            ? "HIGH_RISK_OVERRIDE:" + overrideRule 
            : buildReason(results, action);

        return DecisionEvent.builder()
            .eventId(UUID.randomUUID())
            .schemaVersion("v1")
            .occurredAt(Instant.now())
            .transactionId(UUID.fromString(event.getTransactionId()))
            .cardLast4(event.getCardLast4())
            .correlationId(event.getCorrelationId())
            .action(action)
            .score(normalizedScore)
            .triggeredRules(results)
            .reason(reason)
            .expiresAt(calculateExpiry(action))
            .build();
    }

    private String buildReason(List<RuleResult> results, Action action) {
        List<String> triggered = results.stream()
            .filter(r -> r.score() > 0.1)
            .map(r -> r.ruleName() + "=" + String.format("%.2f", r.score()))
            .toList();
        return String.join(", ", triggered);
    }

    private Instant calculateExpiry(Action action) {
        return switch (action) {
            case ALLOW -> Instant.now().plusSeconds(60);
            case FLAG -> Instant.now().plusSeconds(300);
            case THROTTLE -> Instant.now().plusSeconds(900);
            case BLOCK -> Instant.now().plusSeconds(3600);
        };
    }
}