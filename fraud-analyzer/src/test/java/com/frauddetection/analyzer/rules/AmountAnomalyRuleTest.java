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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AmountAnomalyRuleTest {

    @Mock
    private RedisStore redisStore;

    private AmountAnomalyRule amountAnomalyRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        amountAnomalyRule = new AmountAnomalyRule(redisStore);
    }

    @Test
    void shouldReturnZeroWhenInsufficientHistory() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.getRecentAmounts(anyString(), anyInt())).thenReturn(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));

        RuleResult result = amountAnomalyRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertTrue(result.details().contains("Insufficient history"));
    }
}