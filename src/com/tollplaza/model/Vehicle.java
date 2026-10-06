package com.tollplaza.model;

/**
 * Abstract class representing a vehicle at the toll plaza.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class Vehicle {
    private String registrationNumber;
    private VehicleCategory category;
    private TollTag tollTag;

    // Parameterized constructor to initialize vehicle details
    public Vehicle(String registrationNumber, VehicleCategory category) {
        this.registrationNumber = registrationNumber;
        this.category = category;
        this.tollTag = null;
    }

    // Abstract method to calculate fee based on vehicle type
    public abstract double calculateTollFee();

    // Check if vehicle has an active tag
    public boolean hasTollTag() {
        return this.tollTag != null;
    }

    // Getters and Setters
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
    }

    public TollTag getTollTag() {
        return tollTag;
    }

    public void setTollTag(TollTag tollTag) {
        this.tollTag = tollTag;
    }

    @Override
    public String toString() {
        return "Vehicle [Number=" + registrationNumber + ", Category=" + category + "]";
    }
}
