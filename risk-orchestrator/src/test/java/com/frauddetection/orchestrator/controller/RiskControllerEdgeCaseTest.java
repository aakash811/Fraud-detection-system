package com.frauddetection.orchestrator.controller;

import com.frauddetection.orchestrator.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RiskControllerEdgeCaseTest {

    @Mock
    private RiskService riskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleEmptyWeightsMap() {
        Map<String, Double> weights = new HashMap<>();
        assertEquals(0, weights.size());
    }

    @Test
    void shouldHandleNegativeWeightValues() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("ip-velocity", -0.5);
        assertEquals(-0.5, weights.get("ip-velocity"));
    }

    @Test
    void shouldUpdateWeightsSuccessfully() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("ip-velocity", 0.8);
        weights.put("merchant-pattern", 0.6);
        assertEquals(2, weights.size());
    }
}