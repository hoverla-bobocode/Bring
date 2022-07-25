package com.bobocode.hoverla.bring.exception;

import com.bobocode.hoverla.bring.annotation.Bean;

/**
 * Thrown to indicate that a class annotated with {@link Bean} annotation has invalid declaration and/or definition.
 */
public class BeanClassValidationException extends RuntimeException {

    public BeanClassValidationException(String message) {
        super(message);
    }
}
