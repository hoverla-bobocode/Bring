package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.exception.BeanAlreadyExistsException;

/**
 * Represents API to work with IoC container, provides read and write operations.
 * Class for registration are not supposed and not forced to be marked as {@link Bean} initially
 */
public interface ApplicationContext extends BeanFactory {

    /**
     * Adds bean to the context. Class added here is not supposed and not forced
     * to be marked as {@link Bean} initially
     *
     * @param name     name of bean to be instantiated and registered
     * @param beanType type of bean to be instantiated and registered
     * @throws BeanAlreadyExistsException if bean with provided name already exists in the context
     */
    <T> void register(String name, Class<T> beanType);

    /**
     * Adds provided beans to the context.
     *
     * @param beanTypes type of bean to be instantiated and registered
     * @throws BeanAlreadyExistsException if at least one bean with a name equals to bean class type full name e.g.
     *                                    com.package.BeanType exists
     */
    void register(Class<?>... beanTypes);

    /**
     * Adds bean to the context. Class added here is not supposed and not forced
     * to be marked as {@link Bean} initially
     *
     * @param beanType type of bean to be instantiated and registered
     * @throws BeanAlreadyExistsException if bean with a  name equals to bean class type full name e.g.
     *                                    com.package.BeanType exists
     */
    <T> String register(Class<T> beanType);
}