package com.tollplaza.model;

import com.tollplaza.exception.InvalidPaymentException;

/**
 * TollTag represents a prepaid electronic FASTag.
 * Demonstrates Encapsulation and business validation.
 */
public class TollTag {
    private String tagId;
    private String vehicleNumber;
    private double balance;

    // Parameterized constructor to initialize the tag
    public TollTag(String tagId, String vehicleNumber, double balance) {
        this.tagId = tagId;
        this.vehicleNumber = vehicleNumber;
        this.balance = balance;
    }

    // Deduct toll fee from balance
    public void deductAmount(double amount) throws InvalidPaymentException {
        if (amount <= 0) {
            throw new InvalidPaymentException("Amount to deduct must be greater than zero.");
        }
        if (this.balance < amount) {
            throw new InvalidPaymentException("Insufficient tag balance! Current: Rs. " + this.balance + ", Required: Rs. " + amount);
        }
        this.balance = this.balance - amount;
    }

    // Add money to tag balance
    public void recharge(double amount) throws InvalidPaymentException {
        if (amount <= 0) {
            throw new InvalidPaymentException("Recharge amount must be greater than zero.");
        }
        this.balance = this.balance + amount;
    }

    // Getters and Setters
    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "TollTag [ID=" + tagId + ", Vehicle=" + vehicleNumber + ", Balance=Rs. " + balance + "]";
    }
}
