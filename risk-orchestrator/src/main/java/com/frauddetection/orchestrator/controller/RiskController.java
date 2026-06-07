package com.frauddetection.orchestrator.controller;

import com.frauddetection.orchestrator.model.RuleConfiguration;
import com.frauddetection.orchestrator.service.RiskService;
import com.frauddetection.orchestrator.service.RiskService.DecisionResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @GetMapping("/decision")
    @Operation(summary = "Get current decision for a card or IP")
    public ResponseEntity<DecisionResponse> getDecision(
            @RequestParam(name = "cardLast4", required = false) String cardLast4,
            @RequestParam(name = "ipAddress", required = false) String ipAddress) {
        DecisionResponse response = riskService.getDecision(cardLast4, ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rules")
    @Operation(summary = "Get current rule configuration")
    public ResponseEntity<RuleConfiguration> getRules() {
        return ResponseEntity.ok(riskService.getCurrentConfiguration());
    }

    @PutMapping("/rules/weights")
    @Operation(summary = "Update rule weights dynamically")
    public ResponseEntity<Void> updateWeights(@RequestBody Map<String, Double> weights) {
        riskService.updateWeights(weights);
        return ResponseEntity.ok().build();
    }
}
