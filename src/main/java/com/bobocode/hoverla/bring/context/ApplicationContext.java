package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.NoSuchBeanException;
import com.bobocode.hoverla.bring.exception.NoUniqueBeanException;

import java.util.Map;

/**
 * Represents API to work with IoC container.
 */
public interface ApplicationContext {
    /**
     * Returns a bean instance by its type.
     *
     * @param beanType type of the desired instance
     * @param <T>      type of the desired instance
     * @return object of type T which represents desired instance
     * @throws NoSuchBeanException   if instance with provided type is not found
     * @throws NoUniqueBeanException if more than one instance with provided type is found
     */
    <T> T getBean(Class<T> beanType);

    /**
     * Returns a bean instance by its name.
     *
     * @param beanName name of the desired instance
     * @return instance object
     * @throws NoSuchBeanException if instance with provided name is not found
     */
    Object getBean(String beanName);

    /**
     * Returns a bean instance by its name and type.
     *
     * @param beanName name of the desired instance
     * @param beanType type of the desired instance
     * @param <T>      type of the desired instance
     * @return object of type T which represents desired instance
     * @throws NoSuchBeanException if instance with provided name and type is not found
     */
    <T> T getBean(String beanName, Class<T> beanType);

    /**
     * Returns a {@link Map} of all beans with the provided type where instance's name is a key and instance's instance is a value.
     *
     * @param beanType type of the desired instance
     * @param <T>      type of the desired instance
     * @return a {@link Map} of all beans with the provided type. If no beans are found returns empty {@link Map}
     */
    <T> Map<String, T> getAllBeans(Class<T> beanType);

    /**
     * Checks whether the bean with the given name is handled by current factory.
     *
     * @param beanName name of the desired instance
     * @return true if the bean name matches, false otherwise
     */
    boolean containsBean(String beanName);
}