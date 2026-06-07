package com.frauddetection.analyzer.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStoreImpl implements RedisStore {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String INCREMENT_WINDOW_SCRIPT = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local cutoff = now - (window * 1000)
        redis.call('ZADD', key, now, now)
        redis.call('ZREMRANGEBYSCORE', key, 0, cutoff)
        return redis.call('ZCARD', key)
        """;

    private static final DefaultRedisScript<Long> INCREMENT_WINDOW = 
        new DefaultRedisScript<>(INCREMENT_WINDOW_SCRIPT, Long.class);

    @Override
    public Long incrementWindowCount(String key, long timestamp, int windowSeconds) {
        long cutoff = timestamp - (windowSeconds * 1000L);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, cutoff);
        redisTemplate.opsForZSet().add(key, timestamp, timestamp);
        return redisTemplate.opsForZSet().count(key, 0, System.currentTimeMillis());
    }

    @Override
    public Boolean isInSet(String setKey, String value) {
        return Optional.ofNullable(redisTemplate.opsForSet().isMember(setKey, value)).orElse(false);
    }

    @Override
    public void saveLocation(String cardLast4, Double lat, Double lon, long timestamp) {
        String key = "txn:geo:" + cardLast4;
        String fieldKey = "last:" + timestamp;
        redisTemplate.opsForHash().put(key, fieldKey, lat + "," + lon);
        redisTemplate.opsForValue().set(key, lat + "," + lon, Duration.ofDays(1));
    }

    @Override
    public GeoLocation getLastLocation(String cardLast4) {
        String key = "txn:geo:" + cardLast4;
        String data = (String) redisTemplate.opsForValue().get(key);
        if (data == null) {
            return null;
        }
        try {
            String[] parts = data.split(",");
            String timestampStr = (String) redisTemplate.opsForHash()
                .get(key, "last:" + parts[0]);
            return new GeoLocation(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), 
                timestampStr != null ? Long.parseLong(timestampStr) : System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("Failed to parse location data: {}", data);
            return null;
        }
    }

    @Override
    public List<Double> getRecentAmounts(String key, int limit) {
        List<Object> range = redisTemplate.opsForList().range(key, 0, limit - 1);
        if (range == null) return List.of();
        return range.stream()
            .filter(obj -> obj instanceof Number n && !Double.isNaN(n.doubleValue()))
            .map(obj -> ((Number) obj).doubleValue())
            .toList();
    }

    @Override
    public void addAmount(String key, double amount) {
        redisTemplate.opsForList().leftPush(key, amount);
        redisTemplate.opsForList().trim(key, 0, 99);
    }

    @Override
    public Long countUniqueCardsForDevice(String key, long now, long windowSeconds) {
        String bucket = String.valueOf(now / (windowSeconds * 1000L));
        String fullKey = key + ":" + bucket;
        return redisTemplate.opsForSet().size(fullKey);
    }

    @Override
    public void addCardToDevice(String key, String cardLast4, long timestamp, long windowSeconds) {
        String bucket = String.valueOf(timestamp / (windowSeconds * 1000L));
        String fullKey = key + ":" + bucket;
        redisTemplate.opsForSet().add(fullKey, cardLast4);
    }

    @Override
    public Long countUniqueMerchantsForCard(String key, long now, long windowSeconds) {
        String bucket = String.valueOf(now / (windowSeconds * 1000));
        String fullKey = key + ":" + bucket;
        return redisTemplate.opsForSet().size(fullKey);
    }

    @Override
    public void addMerchantToCard(String key, String merchantId, long timestamp, long windowSeconds) {
        String bucket = String.valueOf(timestamp / (windowSeconds * 1000));
        String fullKey = key + ":" + bucket;
        redisTemplate.opsForSet().add(fullKey, merchantId);
    }
}