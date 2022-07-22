package com.bobocode.hoverla.bring.exception;

/**
 * Thrown to indicate that there are conflicts during validation
 */
public class BeanValidationException extends RuntimeException {
    public BeanValidationException(String message) {
        super(message);
    }
}
