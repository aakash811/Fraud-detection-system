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
public class DeviceFingerprintRule implements DetectionRule {

    private static final double WEIGHT = 0.10;
    private static final int CARD_THRESHOLD = 3;
    private static final long TIME_WINDOW_SECONDS = 3600; // 1 hour

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "device-fingerprint";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.getDeviceId() == null || event.getDeviceId().isBlank()) {
            return RuleResult.of(getName(), 0.0, WEIGHT, "No device fingerprint");
        }
        
        String key = "device:cards:" + event.getDeviceId();
        redisStore.addCardToDevice(key, event.getCardLast4(), event.getOccurredAt().toEpochMilli(), TIME_WINDOW_SECONDS);
        
        Long uniqueCards = redisStore.countUniqueCardsForDevice(key, event.getOccurredAt().toEpochMilli(), TIME_WINDOW_SECONDS);
        
        if (uniqueCards > CARD_THRESHOLD) {
            double score = Math.min(1.0, (uniqueCards - CARD_THRESHOLD) / 10.0 + 0.5);
            return RuleResult.of(getName(), score, WEIGHT, 
                "Device used with " + uniqueCards + " cards in 1 hour");
        }
        
        return RuleResult.of(getName(), 0.0, WEIGHT, "Device fingerprint normal");
    }
}