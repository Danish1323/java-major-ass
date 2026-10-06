package com.tollplaza.service;

import com.tollplaza.exception.InvalidPaymentException;
import com.tollplaza.exception.InvalidVehicleException;

/**
 * Utility class to validate inputs before processing toll transactions.
 */
public class Validator {

    // Validate vehicle registration number
    public static void validateVehicleNumber(String vehicleNumber) throws InvalidVehicleException {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            throw new InvalidVehicleException("Vehicle number cannot be empty.");
        }
        if (vehicleNumber.trim().length() < 4) {
            throw new InvalidVehicleException("Vehicle number must be at least 4 characters.");
        }
    }

    // Validate Toll Tag ID
    public static void validateTagId(String tagId) throws InvalidPaymentException {
        if (tagId == null || tagId.trim().isEmpty()) {
            throw new InvalidPaymentException("Toll Tag ID cannot be empty.");
        }
    }

    // Validate fee amount using Wrapper class
    public static void validateFeeAmount(Double amount) throws InvalidPaymentException {
        if (amount == null || amount <= 0.0) {
            throw new InvalidPaymentException("Fee amount must be greater than zero.");
        }
    }

    // Validate payment mode (must be CASH or TAG)
    public static void validatePaymentMode(String mode) throws InvalidPaymentException {
        if (mode == null) {
            throw new InvalidPaymentException("Payment mode cannot be null.");
        }
        if (!mode.equalsIgnoreCase("CASH") && !mode.equalsIgnoreCase("TAG")) {
            throw new InvalidPaymentException("Payment mode must be CASH or TAG.");
        }
    }
}
