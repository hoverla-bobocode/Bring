package com.bobocode.hoverla.bring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify bean name. Used only for parameters.
 *
 * <p>Name specified in {@link Qualifier @Qualifier} takes precedence over other name specifications,
 * so that parameter marked with {@link Qualifier @Qualifier} will always indicate bean with the name
 * taken from {@link Qualifier#value()}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Bean
 * public class MyBean {
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
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {

    /**
     * Bean name, required to specify.
     */
    String value();
}
