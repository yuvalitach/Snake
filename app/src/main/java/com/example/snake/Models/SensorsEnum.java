package com.example.snake.Models;

public enum SensorsEnum {
    withoutSensors (0),
    withSensors (1);

    private final int value;

    SensorsEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
