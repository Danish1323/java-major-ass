package com.tollplaza.exception;

/**
 * Exception thrown when vehicle details are invalid.
 */
public class InvalidVehicleException extends Exception {

    public InvalidVehicleException(String message) {
        super(message);
    }
}
