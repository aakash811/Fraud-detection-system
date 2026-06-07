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
public class BlacklistRule implements DetectionRule {

    private static final double WEIGHT = 0.30;
    private static final String BLACKLIST_KEY = "rule:blacklist:cards";

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "blacklist";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.getCardLast4() == null) {
            return RuleResult.of(getName(), 0.0, WEIGHT, "No card provided");
        }
        
        Boolean isBlacklisted = redisStore.isInSet(BLACKLIST_KEY, event.getCardLast4());
        
        if (Boolean.TRUE.equals(isBlacklisted)) {
            return RuleResult.of(getName(), 0.95, WEIGHT, "Card " + event.getCardLast4() + " is blacklisted");
        }
        
        return RuleResult.of(getName(), 0.0, WEIGHT, "Card not blacklisted");
    }
}