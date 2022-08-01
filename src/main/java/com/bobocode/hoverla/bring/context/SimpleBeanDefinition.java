package com.bobocode.hoverla.bring.context;

import java.util.Collections;

public class SimpleBeanDefinition<T> extends AbstractBeanDefinition {

    public SimpleBeanDefinition(T instance) {
        type = instance.getClass();
        name = type.getName();
        dependencies = Collections.emptyMap();
        super.instance = instance;
    }

    public SimpleBeanDefinition(String name, T instance) {
        super.name = name;
        type = instance.getClass();
        dependencies = Collections.emptyMap();
        super.instance = instance;
    }

    @Override
    public void instantiate(BeanDefinition... dependencies) {
        // no-op
    }
}
