package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;

import java.util.Map;

/**
 * Describes a bean instance, which has {@code name}, {@code type}, {@code dependencies} and further information supplied by
 * concrete implementations.
 *
 * @see Bean @Bean
 * @see AbstractBeanDefinition
 */
public interface BeanDefinition {

    /**
     * Method for getting name of current {@link BeanDefinition}.
     *
     * @return name of current {@link BeanDefinition}
     */
    String name();

    /**
     * Method for getting type of current {@link BeanDefinition}.
     *
     * @return type of current {@link BeanDefinition}
     */
    Class<?> type();

    /**
     * Returns dependencies of current {@link BeanDefinition} that are required for its instantiation in a format of {@link Map}.
     * <p>Key in this map is a {@link String} object that represents name of dependent {@link BeanDefinition}.</p>
     * <p>Value is this map is a {@link Class} object that represents type of dependent {@link BeanDefinition}.</p>
     *
     * @return map of names and types of dependent {@link BeanDefinition}s.
     */
    Map<String, Class<?>> dependencies();

    /**
     * Handy method to check whether a bean instance has already been created from current {@link BeanDefinition}.
     *
     * @return {@code true} if bean was instantiated, {@code false} otherwise.
     */
    boolean isInstantiated();

    /**
     * Instantiates a bean from current {@link BeanDefinition} using all necessary information encapsulated within.
     *
     * <p>Requires {@link BeanDefinition}s that are treated as dependencies to be passed as arguments.</p>
     * <p>If arguments don't match the actual required dependencies, an exception will be thrown.</p>
     *
     * @param dependencies {@link BeanDefinition} objects that are treated as required dependencies for
     *                     instantiating a bean from current {@link BeanDefinition}
     */
    void instantiate(BeanDefinition... dependencies);

    /**
     * Method for getting a created bean instance of current {@link BeanDefinition}.
     *
     * @return bean instance of current {@link BeanDefinition}.
     * @throws NullPointerException when instance of current {@link BeanDefinition} is null.
     */
    Object getInstance();

}
