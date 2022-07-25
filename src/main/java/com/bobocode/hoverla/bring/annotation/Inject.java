package com.bobocode.hoverla.bring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a constructor or field to be used during dependency injection.
 *
 * <p>Usage of this annotation tells the container to lookup for the corresponding field(s) or/and constructor parameter(s)
 * and inject them into the class that uses {@link Inject @Inject}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Bean
 * public class MyBean {
 *
 *    @Inject
 *    private OtherBean otherBean;
 *
 *    private final AnotherBean anotherBean;
 *
 *    @Inject
 *    public MyBean(AnotherBean anotherBean) {
 *      this.anotherBean = anotherBean;
 *    }
 *
 * }}
 * </pre>
 *
 * Only one constructor of any given bean class may declare this annotation.
 * <p>In case no such constructor found a plain one will be used for injection.</p>
 * <p>In case plain constructor has no arguments no injection will happen.</p>
 *
 * @see Bean @Bean
 * @see Qualifier @Qualifier
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
