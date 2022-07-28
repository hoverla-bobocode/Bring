package com.bobocode.hoverla.bring.exception;

/**
 * Thrown to indicate that bean definition failed to instantiate a bean
 */
public class BeanInstanceCreationException extends RuntimeException {

    public BeanInstanceCreationException(String message) {
        super(message);
    }

    public BeanInstanceCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
