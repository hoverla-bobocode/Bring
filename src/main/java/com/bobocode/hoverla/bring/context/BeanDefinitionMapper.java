package com.bobocode.hoverla.bring.context;

public class BeanDefinitionMapper {

    public BeanDefinition mapToBeanDefinition(Class<?> scannedClass) {
        return new ClassBasedBeanDefinition(scannedClass);
    }

}
