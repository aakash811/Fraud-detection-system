package com.frauddetection.common.model;

public enum Action {
    ALLOW(1),
    FLAG(2),
    THROTTLE(3),
    BLOCK(4);

    private final int priority;

    Action(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static Action fromScore(double score) {
        if (score < 0.30) {
            return ALLOW;
        } else if (score < 0.50) {
            return FLAG;
        } else if (score < 0.75) {
            return THROTTLE;
        } else {
            return BLOCK;
        }
    }
}