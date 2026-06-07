package com.frauddetection.analyzer.rules;

import com.frauddetection.common.model.RuleResult;
import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IpVelocityRule implements DetectionRule {

    private static final double WEIGHT = 0.15;
    private static final int IP_THRESHOLD = 20;
    private static final int WINDOW_SECONDS = 300;

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "ip-velocity";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.getIpAddress() == null) {
            return RuleResult.of(getName(), 0.0, WEIGHT, "No IP provided");
        }
        
        String key = "txn:ip:" + event.getIpAddress() + ":" + getBucket(event.getOccurredAt());
        Long count = redisStore.incrementWindowCount(key, event.getOccurredAt().getEpochSecond() * 1000, WINDOW_SECONDS);
        
        if (count > IP_THRESHOLD) {
            double score = Math.min(1.0, (count - IP_THRESHOLD) / 30.0 + 0.4);
            return RuleResult.of(getName(), score, WEIGHT, 
                "IP " + event.getIpAddress() + " has " + count + " transactions");
        }
        
        return RuleResult.of(getName(), 0.0, WEIGHT, "Normal IP velocity");
    }

    private long getBucket(java.time.Instant instant) {
        return instant.getEpochSecond() / WINDOW_SECONDS;
    }
}