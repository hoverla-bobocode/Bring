package com.bobocode.hoverla.bring.annotation;

import com.bobocode.hoverla.bring.context.ApplicationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which is used to mark types as beans in order to be scanned by {@link ApplicationContext}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

    /**
     * Represents bean name that can be used to retrieve it using e.g. {@link ApplicationContext#getBean(String)}
     * or other methods that require bean name
     */
    String name() default "";
}
