package com.bobocode.hoverla.bring.context;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BeanDefinitionsContainer {

    private final Map<String, BeanDefinition> beanDefinitions;

    public BeanDefinitionsContainer(List<BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions.stream()
                .collect(Collectors.toMap(BeanDefinition::name, Function.identity()));
    }

    public Optional<BeanDefinition> getBeanDefinitionByName(String name) {
        return Optional.ofNullable(beanDefinitions.get(name));
    }

    public List<BeanDefinition> getBeansAssignableFromType(Class<?> type) {
        return beanDefinitions.values()
                .stream()
                .filter(b -> type.isAssignableFrom(b.type()))
                .toList();
    }

    public List<BeanDefinition> getBeansWithExactType(Class<?> type) {
        return beanDefinitions.values()
                .stream()
                .filter(b -> type.equals(b.type()))
                .toList();
    }

    public Collection<BeanDefinition> getBeanDefinitions() {
        return beanDefinitions.values();
    }
}
