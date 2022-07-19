package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

import java.lang.reflect.Method;

public class BeanDefinitionMapper {

    public BeanDefinition mapToBeanDefinition(Class<?> scannedClass) {
        return new ClassBasedBeanDefinition(scannedClass);
    }

    /**
     * Maps bean configuration class instance and its bean method to {@link BeanDefinition}
     *
     * @param configInstance beans config class marked with {@link Configuration @Configuration} instance
     * @param beanMethod     bean method marked with {@link Bean @Bean}
     * @return instance of {@link ConfigBasedBeanDefinition} via polymorphic {@link BeanDefinition} reference
     */
    public BeanDefinition mapToBeanDefinition(Object configInstance, Method beanMethod) {
        return new ConfigBasedBeanDefinition(configInstance, beanMethod);
    }

}
