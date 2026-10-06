package com.tollplaza.model;

/**
 * Enum representing different categories of vehicles passing through the toll plaza.
 * Each category has a default toll fee associated with it.
 */
public enum VehicleCategory {
    CAR(100.0),
    TRUCK(250.0),
    BUS(200.0),
    TWO_WHEELER(50.0);

    // Default rate for this vehicle category
    private final double baseRate;

    // Enum constructor to initialize base rate
    VehicleCategory(double baseRate) {
        this.baseRate = baseRate;
    }

    // Getter method to retrieve base rate
    public double getBaseRate() {
        return this.baseRate;
    }
}
