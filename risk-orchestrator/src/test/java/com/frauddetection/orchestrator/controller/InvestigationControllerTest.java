package com.frauddetection.orchestrator.controller;

import com.frauddetection.orchestrator.service.InvestigationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class InvestigationControllerTest {

    @Mock
    private InvestigationService investigationService;

    private InvestigationController investigationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        investigationController = new InvestigationController(investigationService);
    }

    @Test
    void shouldGetPendingInvestigations() {
        when(investigationService.getPendingInvestigations()).thenReturn(List.of());
        ResponseEntity<List<InvestigationDto>> response = investigationController.getPendingInvestigations();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldGetCardHistory() {
        when(investigationService.getCardHistory("1234")).thenReturn(List.of());
        ResponseEntity<List<InvestigationDto>> response = investigationController.getCardHistory("1234");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldGetStats() {
        when(investigationService.getDecisionStats()).thenReturn(List.of());
        ResponseEntity<List<StatDto>> response = investigationController.getStats();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldAddToBlacklist() {
        ResponseEntity<Void> response = investigationController.addToBlacklist("1234");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldRemoveFromBlacklist() {
        ResponseEntity<Void> response = investigationController.removeFromBlacklist("1234");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldUpdateStatus() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus("IN_REVIEW");
        request.setNotes("Updated notes");
        ResponseEntity<Void> response = investigationController.updateStatus(UUID.randomUUID(), request);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldHandleShortCardLast4() {
        ResponseEntity<Void> response = investigationController.addToBlacklist("12");
        assertEquals(200, response.getStatusCode().value());
    }
}