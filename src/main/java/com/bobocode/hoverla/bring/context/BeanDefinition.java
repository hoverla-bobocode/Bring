package com.bobocode.hoverla.bring.context;

import java.util.Map;

/**
 * Wraps all necessary bean information, encapsulates bean creation logic (this logic can be seperated in future)
 */
public interface BeanDefinition {
    /**
     * Returns bean name
     * @return bean name
     */
    String name();

    /**
     * Returns bean instance type
     * @return bean instance type
     */
    Class<?> type();

    /**
     * Returns bean dependencies that are required for its instantiation in a format of Map where key
     * is dependency's name and value is dependencies type
     * @return bean dependencies map
     */
    Map<String, Class<?>> dependencies();

    /**
     * Instantiates bean using all necessary information encapsulated within.
     * Requires bean definition which are treated as dependencies to be passed as arguments.
     * If arguments don't match the actual required dependencies, the exception will be thrown.
     *
     * @param dependencies bean definitions that are treated as required dependencies for this bean instantiation
     * @return bean instance
     */
    Object instance(BeanDefinition... dependencies);
}
