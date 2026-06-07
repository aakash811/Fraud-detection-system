package com.frauddetection.analyzer.rules;

import com.frauddetection.analyzer.store.GeoLocation;
import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GeolocationRuleTest {

    @Mock
    private RedisStore redisStore;

    private GeolocationRule geolocationRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        geolocationRule = new GeolocationRule(redisStore);
    }

    @Test
    void shouldAllowFirstLocation() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .latitude(40.7128)
            .longitude(-74.0060)
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.getLastLocation(eq("1234"))).thenReturn(null);

        RuleResult result = geolocationRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertEquals("geolocation", result.ruleName());
    }

    @Test
    void shouldFlagImpossibleTravel() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now().plusSeconds(3600))
            .cardLast4("1234")
            .latitude(40.7128)
            .longitude(-74.0060)
            .transactionId(UUID.randomUUID().toString())
            .build();

        GeoLocation prevLocation = new GeoLocation(34.0522, -118.2437, Instant.now().getEpochSecond() * 1000);
        when(redisStore.getLastLocation(eq("1234"))).thenReturn(prevLocation);

        RuleResult result = geolocationRule.evaluate(event);

        assertTrue(result.score() > 0);
        assertTrue(result.details().contains("Impossible travel"));
    }

    @Test
    void shouldSkipWhenNoLocationProvided() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = geolocationRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertEquals("Location not provided", result.details());
    }
}