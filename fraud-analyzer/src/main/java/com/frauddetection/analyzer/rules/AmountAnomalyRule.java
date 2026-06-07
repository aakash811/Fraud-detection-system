package com.frauddetection.analyzer.rules;

import com.frauddetection.common.model.RuleResult;
import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AmountAnomalyRule implements DetectionRule {

    private static final double WEIGHT = 0.20;
    private static final int HISTORY_SIZE = 50;
    private static final double THRESHOLD_STD_DEVIATIONS = 2.5;

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "amount-anomaly";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        String key = "txn:amounts:" + event.getCardLast4();
        List<Double> amounts = redisStore.getRecentAmounts(key, HISTORY_SIZE);
        
        if (amounts.size() < 10) {
            redisStore.addAmount(key, event.getAmount().doubleValue());
            return RuleResult.of(getName(), 0.0, WEIGHT, "Insufficient history for anomaly detection");
        }
        
        double currentAmount = event.getAmount().doubleValue();
        
        DoubleSummaryStatistics stats = amounts.stream()
            .mapToDouble(Double::doubleValue)
            .summaryStatistics();
        
        double mean = stats.getAverage();
        double variance = amounts.stream()
            .mapToDouble(Double::doubleValue)
            .map(a -> Math.pow(a - mean, 2))
            .average()
            .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        double zscore = stdDev > 0 ? Math.abs(currentAmount - mean) / stdDev : 0;
        
        redisStore.addAmount(key, currentAmount);
        
        if (zscore > THRESHOLD_STD_DEVIATIONS) {
            double score = Math.min(1.0, zscore / 5.0);
            return RuleResult.of(getName(), score, WEIGHT, 
                "Amount " + currentAmount + " is " + zscore + " std devs from mean");
        }
        
        return RuleResult.of(getName(), 0.0, WEIGHT, "Amount within normal range");
    }
}