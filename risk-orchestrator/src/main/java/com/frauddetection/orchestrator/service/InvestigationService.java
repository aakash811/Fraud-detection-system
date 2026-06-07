package com.frauddetection.orchestrator.service;

import com.frauddetection.common.model.Action;
import com.frauddetection.common.model.InvestigationStatus;
import com.frauddetection.orchestrator.controller.InvestigationDto;
import com.frauddetection.orchestrator.controller.StatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestigationService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_KEY = "rule:blacklist:cards";
    private static final String DECISION_PREFIX = "decision:";

    public List<InvestigationDto> getPendingInvestigations() {
        List<InvestigationDto> investigations = new ArrayList<>();
        try {
            var keys = redisTemplate.keys(DECISION_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    var fields = redisTemplate.opsForHash().entries(key);
                    String action = getAsString(fields.get("action"));
                    if ("FLAG".equals(action)) {
                        investigations.add(new InvestigationDto(
                            UUID.randomUUID(),
                            null,
                            getAsString(fields.get("cardLast4")),
                            Action.valueOf(action),
                            getAsDouble(fields.get("score")),
                            getAsString(fields.get("reason")),
                            getAsString(fields.get("correlationId")),
                            InvestigationStatus.PENDING
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve investigations: {}", e.getMessage());
        }
        return investigations;
    }

    public List<InvestigationDto> getCardHistory(String cardLast4) {
        List<InvestigationDto> history = new ArrayList<>();
        try {
            String key = DECISION_PREFIX + cardLast4;
            var fields = redisTemplate.opsForHash().entries(key);
            if (!fields.isEmpty()) {
                history.add(new InvestigationDto(
                    null,
                    null,
                    cardLast4,
                    Action.valueOf(getAsString(fields.get("action"))),
                    getAsDouble(fields.get("score")),
                    getAsString(fields.get("reason")),
                    getAsString(fields.get("correlationId")),
                    InvestigationStatus.PENDING
                ));
            }
        } catch (Exception e) {
            log.warn("Could not retrieve card history: {}", e.getMessage());
        }
        return history;
    }

    public List<StatDto> getDecisionStats() {
        List<StatDto> stats = new ArrayList<>();
        try {
            String pattern = DECISION_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                long allow = 0, flag = 0, throttle = 0, block = 0;
                for (String key : keys) {
                    var fields = redisTemplate.opsForHash().entries(key);
                    String action = getAsString(fields.get("action"));
                    if (action != null) {
                        switch (action) {
                            case "ALLOW" -> allow++;
                            case "FLAG" -> flag++;
                            case "THROTTLE" -> throttle++;
                            case "BLOCK" -> block++;
                        }
                    }
                }
                stats.add(new StatDto(Action.ALLOW, allow));
                stats.add(new StatDto(Action.FLAG, flag));
                stats.add(new StatDto(Action.THROTTLE, throttle));
                stats.add(new StatDto(Action.BLOCK, block));
            }
        } catch (Exception e) {
            log.warn("Could not retrieve decision stats: {}", e.getMessage());
        }
        return stats;
    }

    public void updateStatus(UUID eventId, String status, String notes) {
        log.info("Investigation {} status updated to {}, notes: {}", eventId, status, notes);
    }

    public void addToBlacklist(String cardLast4) {
        try {
            redisTemplate.opsForSet().add(BLACKLIST_KEY, cardLast4);
            log.info("Added card {} to blacklist", cardLast4);
        } catch (Exception e) {
            log.error("Failed to add card to blacklist: {}", e.getMessage());
            throw new RuntimeException("Redis unavailable: " + e.getMessage());
        }
    }

    public void removeFromBlacklist(String cardLast4) {
        try {
            redisTemplate.opsForSet().remove(BLACKLIST_KEY, cardLast4);
            log.info("Removed card {} from blacklist", cardLast4);
        } catch (Exception e) {
            log.error("Failed to remove card from blacklist: {}", e.getMessage());
        }
    }

    private String getAsString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String s) return s;
        return obj.toString();
    }

    private Double getAsDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}