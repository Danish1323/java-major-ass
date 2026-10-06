package com.tollplaza.model;

/**
 * StandardVehicle is a concrete subclass of Vehicle.
 * Demonstrates Inheritance and Method Overriding.
 */
public class StandardVehicle extends Vehicle {

    // Constructor calling the superclass constructor
    public StandardVehicle(String registrationNumber, VehicleCategory category) {
        super(registrationNumber, category);
    }

    // Overriding the abstract calculateTollFee method
    @Override
    public double calculateTollFee() {
        if (getCategory() != null) {
            return getCategory().getBaseRate();
        }
        return 100.0;
    }
}
