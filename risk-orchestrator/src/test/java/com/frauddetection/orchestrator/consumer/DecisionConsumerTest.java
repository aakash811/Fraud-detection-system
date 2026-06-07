package com.frauddetection.orchestrator.consumer;

import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.model.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionConsumerTest {

    private DecisionConsumer decisionConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleDecisionEvent() {
        DecisionEvent event = DecisionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now().plusSeconds(3600))
            .transactionId(UUID.randomUUID())
            .cardLast4("1234")
            .action(Action.ALLOW)
            .score(0.1)
            .reason("Test decision")
            .build();
        
        assertNotNull(event.getCardLast4());
    }
}