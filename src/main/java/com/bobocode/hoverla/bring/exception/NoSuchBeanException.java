package com.bobocode.hoverla.bring.exception;

import com.bobocode.hoverla.bring.context.ApplicationContext;

/**
 * Thrown to indicate that no bean with provided attributes is found in {@link ApplicationContext}
 */
public class NoSuchBeanException extends RuntimeException {
}
