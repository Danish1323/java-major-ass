package com.tollplaza.exception;

/**
 * Exception thrown when payment fails or tag balance is insufficient.
 */
public class InvalidPaymentException extends Exception {

    public InvalidPaymentException(String message) {
        super(message);
    }
}
