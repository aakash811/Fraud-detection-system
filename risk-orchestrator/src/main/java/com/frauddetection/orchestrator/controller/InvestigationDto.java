package com.frauddetection.orchestrator.controller;

import com.frauddetection.common.model.Action;
import com.frauddetection.common.model.InvestigationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestigationDto {
    private UUID eventId;
    private UUID transactionId;
    private String cardLast4;
    private Action action;
    private Double score;
    private String reason;
    private String correlationId;
    private InvestigationStatus investigationStatus;
}