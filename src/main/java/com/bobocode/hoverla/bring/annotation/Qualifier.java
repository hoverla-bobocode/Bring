package com.bobocode.hoverla.bring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify bean name. Used only for parameters.
 * Name specified in {@link Qualifier} takes precedence over other name specifications,
 * so that parameter marked with {@link Qualifier} will always indicate bean with the name
 * taken from {@link Qualifier#value()}
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {
    /**
     * Bean name, required to specify
     */
    String value();
}
