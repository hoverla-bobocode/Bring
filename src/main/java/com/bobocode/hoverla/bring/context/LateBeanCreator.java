package com.bobocode.hoverla.bring.context;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LateBeanCreator {

    private final BeanAnnotationClassValidator validator;
    private final BeanDefinitionMapper mapper;

    public <T> BeanDefinition createLateBean(String name, Class<T> beanType) {
        validator.validateClass(beanType);
        return mapper.mapToBeanDefinition(name, beanType);
    }

    public <T> BeanDefinition createLateBean(Class<T> beanType) {
        validator.validateClass(beanType);
        return mapper.mapToBeanDefinition(beanType);
    }
}
