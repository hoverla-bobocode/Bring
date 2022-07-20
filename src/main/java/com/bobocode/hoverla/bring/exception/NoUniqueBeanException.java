package com.bobocode.hoverla.bring.exception;

/**
 * Thrown to indicate that bean is not unique
 */
public class NoUniqueBeanException extends RuntimeException {
    public NoUniqueBeanException(String message) {
        super(message);
    }
}
