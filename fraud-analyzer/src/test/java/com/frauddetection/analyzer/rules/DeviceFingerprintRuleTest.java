package com.frauddetection.analyzer.rules;

import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeviceFingerprintRuleTest {

    @Mock
    private RedisStore redisStore;

    private DeviceFingerprintRule deviceFingerprintRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deviceFingerprintRule = new DeviceFingerprintRule(redisStore);
    }

    @Test
    void shouldReturnZeroScoreWhenBelowThreshold() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .deviceId("device123")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.countUniqueCardsForDevice(anyString(), anyLong(), anyLong())).thenReturn(2L);

        RuleResult result = deviceFingerprintRule.evaluate(event);

        assertEquals(0.0, result.score());
    }

    @Test
    void shouldFlagHighDeviceUsage() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .deviceId("device123")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.countUniqueCardsForDevice(anyString(), anyLong(), anyLong())).thenReturn(5L);

        RuleResult result = deviceFingerprintRule.evaluate(event);

        assertTrue(result.score() > 0);
        assertTrue(result.details().contains("Device used with"));
    }

    @Test
    void shouldSkipWhenNoDeviceProvided() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = deviceFingerprintRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertEquals("No device fingerprint", result.details());
    }
}