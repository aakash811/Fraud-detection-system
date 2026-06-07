package com.frauddetection.analyzer.rules;

import com.frauddetection.common.model.RuleResult;
import com.frauddetection.common.event.TransactionEvent;

public interface DetectionRule {
    String getName();
    double getWeight();
    RuleResult evaluate(TransactionEvent event);
}