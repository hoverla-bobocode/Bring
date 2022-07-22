package com.bobocode.hoverla.bring.exception;

/**
 * Thrown to indicate that context already has a bean with the same name
 */
public class BeanAlreadyExistsException extends RuntimeException {

    public BeanAlreadyExistsException(String message) {
        super(message);
    }
}
