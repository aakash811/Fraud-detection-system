package com.frauddetection.orchestrator.controller;

import com.frauddetection.common.model.Action;
import lombok.Value;

@Value
public class StatDto {
    Action action;
    Long count;
}