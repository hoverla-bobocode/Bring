package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

import java.lang.reflect.Method;

public class BeanDefinitionMapper {

    /**
     * Maps Java class annotated with {@link Bean @Bean} to {@link BeanDefinition}.
     *
     * @param beanClass instance of {@link Class} annotated with {@link Bean @Bean} annotation.
     * @return instance of {@link ClassBasedBeanDefinition} via polymorphic {@link BeanDefinition} reference
     */
    public BeanDefinition mapToBeanDefinition(Class<?> beanClass) {
        return new ClassBasedBeanDefinition(beanClass);
    }

    /**
     * Maps instance of Java class annotated with {@link Configuration @Configuration}
     * and its {@link Method} annotated with {@link Bean @Bean} to {@link BeanDefinition}
     *
     * @param configInstance instance of a class annotated with {@link Configuration @Configuration}
     * @param beanMethod     instance of a method annotated with {@link Bean @Bean}
     * @return instance of {@link ConfigBasedBeanDefinition} via polymorphic {@link BeanDefinition} reference
     */
    public BeanDefinition mapToBeanDefinition(Object configInstance, Method beanMethod) {
        return new ConfigBasedBeanDefinition(configInstance, beanMethod);
    }

    public BeanDefinition mapToBeanDefinition(String name, Class<?> scannedClass) {
        return new ClassBasedBeanDefinition(name, scannedClass);
    }

}
