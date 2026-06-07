package com.frauddetection.common.model;

public record RuleResult(
    String ruleName,
    double score,
    double weight,
    String details
) {
    public static RuleResult of(String ruleName, double score, double weight, String details) {
        return new RuleResult(ruleName, score, weight, details);
    }
}