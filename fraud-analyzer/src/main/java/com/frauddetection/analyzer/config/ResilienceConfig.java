package com.frauddetection.analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreaker redisCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();
        return CircuitBreaker.of("redis", config);
    }

    @Bean
    public CircuitBreaker kafkaCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .slidingWindowSize(20)
            .minimumNumberOfCalls(10)
            .build();
        return CircuitBreaker.of("kafka", config);
    }

    @Bean
    public Retry kafkaRetry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .build();
        return Retry.of("kafka", config);
    }
}