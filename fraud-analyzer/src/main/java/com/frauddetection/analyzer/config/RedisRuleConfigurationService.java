package com.frauddetection.analyzer.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisRuleConfigurationService {
    
    private final StringRedisTemplate redisTemplate;
    private final CircuitBreaker circuitBreaker;
    
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private volatile long lastUpdateTime = 0;
    private static final long CACHE_TTL_MS = 30000;
    
    public RedisRuleConfigurationService(StringRedisTemplate redisTemplate, 
                                       CircuitBreaker redisCircuitBreaker) {
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = redisCircuitBreaker;
    }
    
    public int getMaxTransactionsPerHour(String ruleName) {
        return getCachedOrFetch("rule:" + ruleName + ":max-transactions", 10);
    }
    
    public double getScoreThreshold(String ruleName) {
        return getCachedOrFetch("rule:" + ruleName + ":score-threshold", 0.5);
    }
    
    public int getWindowSeconds(String ruleName) {
        return getCachedOrFetch("rule:" + ruleName + ":window-seconds", 3600);
    }
    
    private <T> T getCachedOrFetch(String key, T defaultValue) {
        if (System.currentTimeMillis() - lastUpdateTime > CACHE_TTL_MS) {
            refreshCache();
        }
        return (T) cache.getOrDefault(key, defaultValue);
    }
    
    private void refreshCache() {
        try {
            if (!circuitBreaker.tryAcquirePermission()) {
                log.warn("Circuit breaker open for Redis rule config, using defaults");
                return;
            }
            
            Map<Object, Object> entries = redisTemplate.opsForHash().entries("fraud-rules-config");
            entries.forEach((k, v) -> cache.put(k.toString(), parseValue(v.toString())));
            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Failed to refresh rule config from Redis", e);
            circuitBreaker.onError(1, java.util.concurrent.TimeUnit.MILLISECONDS, e);
        }
    }
    
    private Object parseValue(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e2) {
                return value;
            }
        }
    }
}