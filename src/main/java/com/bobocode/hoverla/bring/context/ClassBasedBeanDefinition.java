package com.bobocode.hoverla.bring.context;

import java.util.List;
import java.util.Map;

public class ClassBasedBeanDefinition implements BeanDefinition {

    private Object instance;

    private String name;

    private Class<?> type;

    private List<String> dependenciesNames;

    public ClassBasedBeanDefinition(Class<?> beanClass) {
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Class<?> type() {
        return null;
    }

    @Override
    public Map<String, Class<?>> dependencies() {
        return null;
    }

    @Override
    public Object instance(BeanDefinition... dependencies) {
        if (instance != null) {
            return instance;
        }

        return null;
    }
}
