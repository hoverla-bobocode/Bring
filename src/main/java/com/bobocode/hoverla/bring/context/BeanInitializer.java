package com.bobocode.hoverla.bring.context;

import java.util.Map;

public class BeanInitializer {

    /**
     * Instantiates beans and injects dependencies in them
     */
    public void initialize(Map<String, BeanDefinition> beanDefinitions) {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            BeanDefinition beanDefinition = entry.getValue();
            BeanDefinition[] instanceDependencies = beanDefinition.dependenciesNames().stream()
                    .map(beanDefinitions::get)
                    .toArray(BeanDefinition[]::new);
            beanDefinition.instance(instanceDependencies);
        }
    }
}
