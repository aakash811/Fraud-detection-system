package com.frauddetection.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "fraud.rules")
public class RuleConfiguration {
    
    private VelocityVelocity velocity = new VelocityVelocity();
    private ThresholdVelocity threshold = new ThresholdVelocity();
    private DeviceVelocity device = new DeviceVelocity();
    private MerchantVelocity merchant = new MerchantVelocity();
    private TimeBasedVelocity timeBased = new TimeBasedVelocity();
    
    @Data
    public static class VelocityVelocity {
        private int windowSeconds = 3600;
        private int maxTransactions = 10;
        private double scoreThreshold = 0.5;
    }
    
    @Data
    public static class ThresholdVelocity {
        private double amountUsd = 10000.0;
        private double scoreThreshold = 0.7;
    }
    
    @Data
    public static class DeviceVelocity {
        private int windowSeconds = 86400;
        private int maxUniqueCards = 3;
        private double scoreIncrement = 0.2;
    }
    
    @Data
    public static class MerchantVelocity {
        private int windowSeconds = 3600;
        private int maxMerchants = 5;
        private double scoreIncrement = 0.15;
    }
    
    @Data
    public static class TimeBasedVelocity {
        private int startHour = 2;
        private int endHour = 5;
        private double scoreThreshold = 0.6;
    }
}