package com.bobocode.hoverla.bring.exception;

/**
 * Thrown to indicate that bean definition cannot be constructed with passed arguments
 */
public class BeanDefinitionConstructionException extends RuntimeException {
    public BeanDefinitionConstructionException(String message) {
        super(message);
    }
}
