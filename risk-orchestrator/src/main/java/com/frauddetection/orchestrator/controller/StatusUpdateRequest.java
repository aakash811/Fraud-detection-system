package com.frauddetection.orchestrator.controller;

import com.frauddetection.common.model.InvestigationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {
    private String status;
    private String notes;
}