package com.frauddetection.orchestrator.controller;

import com.frauddetection.orchestrator.service.InvestigationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investigations")
@RequiredArgsConstructor
public class InvestigationController {

    private final InvestigationService investigationService;

    @GetMapping
    @Operation(summary = "Get pending investigations for review")
    public ResponseEntity<List<InvestigationDto>> getPendingInvestigations() {
        List<InvestigationDto> investigations = investigationService.getPendingInvestigations();
        return ResponseEntity.ok(investigations);
    }

    @GetMapping("/{cardLast4}")
    @Operation(summary = "Get decision history for a card")
    public ResponseEntity<List<InvestigationDto>> getCardHistory(@PathVariable String cardLast4) {
        List<InvestigationDto> history = investigationService.getCardHistory(cardLast4);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get fraud statistics by action")
    public ResponseEntity<List<StatDto>> getStats() {
        List<StatDto> stats = investigationService.getDecisionStats();
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{eventId}/status")
    @Operation(summary = "Update investigation status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID eventId,
            @RequestBody StatusUpdateRequest request) {
        investigationService.updateStatus(eventId, request.getStatus(), request.getNotes());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/blacklist/{cardLast4}")
    @Operation(summary = "Add card to blacklist")
    public ResponseEntity<Void> addToBlacklist(@PathVariable String cardLast4) {
        investigationService.addToBlacklist(cardLast4);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/blacklist/{cardLast4}")
    @Operation(summary = "Remove card from blacklist")
    public ResponseEntity<Void> removeFromBlacklist(@PathVariable String cardLast4) {
        investigationService.removeFromBlacklist(cardLast4);
        return ResponseEntity.ok().build();
    }
}