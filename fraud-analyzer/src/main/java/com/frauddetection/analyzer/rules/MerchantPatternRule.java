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
public class MerchantPatternRule implements DetectionRule {

    private static final double WEIGHT = 0.10;
    private static final int MERCHANT_COUNT_THRESHOLD = 15;
    private static final long TIME_WINDOW_SECONDS = 1800; // 30 minutes

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "merchant-pattern";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.getMerchantId() == null) {
            return RuleResult.of(getName(), 0.0, WEIGHT, "No merchant info");
        }
        
        String key = "card:merchants:" + event.getCardLast4();
        redisStore.addMerchantToCard(key, event.getMerchantId(), 
            event.getOccurredAt().toEpochMilli(), TIME_WINDOW_SECONDS);
        
        Long uniqueMerchants = redisStore.countUniqueMerchantsForCard(key, 
            event.getOccurredAt().toEpochMilli(), TIME_WINDOW_SECONDS);
        
        if (uniqueMerchants > MERCHANT_COUNT_THRESHOLD) {
            double score = Math.min(1.0, (uniqueMerchants - MERCHANT_COUNT_THRESHOLD) / 20.0 + 0.3);
            return RuleResult.of(getName(), score, WEIGHT, 
                "Rapid merchant switching: " + uniqueMerchants + " merchants in 30 min");
        }
        
        return RuleResult.of(getName(), 0.0, WEIGHT, "Normal merchant pattern");
    }
}