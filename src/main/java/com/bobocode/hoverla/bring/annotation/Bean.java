package com.bobocode.hoverla.bring.annotation;

import com.bobocode.hoverla.bring.context.ApplicationContext;
import com.bobocode.hoverla.bring.context.BeanDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which is used to mark types as beans in order to be scanned by {@link ApplicationContext}.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Bean(name = "beanName")
 * public class MyBean {
 *
 * }}
 * </pre>
 *
 * Having such class in a package for bean scanning will cause {@link ApplicationContext} to
 * scan this class and create a bean out of it.
 * <p>For an example of usage with Java Configuration classes - check {@link Configuration @Configuration}.</p>
 *
 * @see Configuration @Configuration
 * @see Inject @Inject
 * @see ApplicationContext
 * @see BeanDefinition
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

    /**
     * Represents bean name.
     * <p>Can be used to retrieve existing bean instance using {@link ApplicationContext#getBean(String)}
     * or other methods that require bean name.</p>
     */
    String value() default "";

    /**
     * Represents whether a bean is primary or not. In case primary is set
     * to {@code true} this bean will be injected in those beans which don't specify
     * a name in {@link Qualifier @Qualifier} for expected bean dependency.
     * By default, all beans are not primary.
     */
    boolean primary() default false;
}
