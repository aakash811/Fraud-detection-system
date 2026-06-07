package com.frauddetection.analyzer.rules;

import com.frauddetection.common.model.RuleResult;
import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class VelocityRule implements DetectionRule {

    private static final int WINDOW_SIZE = 300; // 5 minutes in seconds
    private static final int THRESHOLD = 10; // Max 10 transactions per window
    private static final double WEIGHT = 0.25;

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "velocity";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        String key = "txn:card:" + event.getCardLast4() + ":" + getBucket(event.getOccurredAt());
        
        Long count = redisStore.incrementWindowCount(key, event.getOccurredAt().toEpochMilli(), WINDOW_SIZE);
        
        if (count > THRESHOLD) {
            double score = Math.min(1.0, (count - THRESHOLD) / 20.0 + 0.5);
            return RuleResult.of(getName(), score, WEIGHT, 
                "Transaction count " + count + " exceeds threshold " + THRESHOLD);
        }
        
        return RuleResult.of(getName(), 0.0, WEIGHT, "Within normal velocity");
    }

    private String getBucket(Instant occurredAt) {
        long seconds = occurredAt.getEpochSecond();
        return String.valueOf(seconds / WINDOW_SIZE);
    }
}