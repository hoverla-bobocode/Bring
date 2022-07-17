package com.bobocode.hoverla.bring.annotation;

import com.bobocode.hoverla.bring.context.ApplicationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark classes that will be treated as Java Configuration bean classes.
 *
 * <pre>
 * Usage:
 * {@code @Configuration
 * class MyBeanConfiguration {
 *     @Bean
 *     public String myBean() {
 *         return "beanInstance";
 *     }
 * }}
 * </pre>
 * Having this class in a package for bean scanning will cause {@link ApplicationContext} to
 * scan this class and create beans from methods that marked as {@link Bean}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
}
