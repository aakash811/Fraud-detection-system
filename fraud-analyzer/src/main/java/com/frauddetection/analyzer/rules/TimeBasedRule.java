package com.frauddetection.analyzer.rules;

import com.frauddetection.common.model.RuleResult;
import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimeBasedRule implements DetectionRule {

    private static final double WEIGHT = 0.15;
    private static final int SUSPICIOUS_START_HOUR = 2;
    private static final int SUSPICIOUS_END_HOUR = 5;

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "time-based";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        ZonedDateTime zonedTime = ZonedDateTime.ofInstant(event.getOccurredAt(), ZoneId.systemDefault());
        int hour = zonedTime.getHour();
        
        if (hour >= SUSPICIOUS_START_HOUR && hour <= SUSPICIOUS_END_HOUR) {
            return RuleResult.of(getName(), 0.6, WEIGHT, 
                "Transaction during suspicious hours: " + hour + ":00");
        }
        
        return RuleResult.of(getName(), 0.0, WEIGHT, "Normal transaction hours");
    }
}