package com.bobocode.hoverla.bring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify bean name. Used for parameters and fields.
 *
 * <p>Name specified in {@link Qualifier @Qualifier} takes precedence over other name specifications,
 * so that element marked with {@link Qualifier @Qualifier} will always indicate bean with the name
 * taken from {@link Qualifier#value()}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Bean
 * public class MyBean {
 *
 *    @Inject
 *    @Qualifier("anotherBean2")
 *    private AnotherBean anotherBean;
 *
 *    private final OtherBean otherBean;
 *
 *    @Inject
 *    public MyBean(@Qualifier("otherBean1") OtherBean otherBean) {
 *      this.otherBean = otherBean;
 *    }
 *
 * }}
 * </pre>
 *
 * @see Bean @Bean
 * @see Configuration @Configuration
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {

    /**
     * Bean name, required to specify.
     */
    String value();
}
