package com.frauddetection.orchestrator.model;

import lombok.Data;
import lombok.Setter;

import java.util.Map;

@Data
@Setter
public class RuleConfiguration {
    private Map<String, Double> weights;
    private Map<String, Double> thresholds;
    private Map<String, String> parameters;
    
    @Data
    public static class Thresholds {
        private double flag;
        private double throttle;
        private double block;
        private double overrideBlock;
    }
}