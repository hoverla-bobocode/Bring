package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class CollectionBeanDefinition extends AbstractBeanDefinition {

    private final Class<?> collectionType;
    private final Class<?> genericType;
    private final String generatedName;

    CollectionBeanDefinition(Class<?> collectionType, Class<?> genericType, List<String> dependencies) {
        if (!Collection.class.isAssignableFrom(collectionType)) {
            throw new BeanInstanceCreationException("Requires collection type to be implementor of Collection interface");
        }
        this.collectionType = collectionType;
        this.genericType = genericType;
        super.dependencies = resolveDependencies(dependencies, genericType);
        this.generatedName = String.valueOf(ThreadLocalRandom.current().nextInt());
    }

    @Override
    public String name() {
        return generatedName;
    }

    @Override
    public Class<?> type() {
        return collectionType;
    }

    @Override
    public Map<String, BeanDependency> dependencies() {
        return dependencies;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public Class<?> collectionGenericType() {
        return genericType;
    }

    @Override
    public void instantiate(BeanDefinition... dependencies) {
        List<Object> instances = Arrays.stream(dependencies)
                .map(BeanDefinition::getInstance)
                .toList();

        if (List.class.isAssignableFrom(collectionType)) {
            instance = instances;
        }
        if (Set.class.isAssignableFrom(collectionType)) {
            instance = instances;
        }
        if (Queue.class.isAssignableFrom(collectionType)) {
            instance = new ArrayDeque<>(instances);
        }
    }

    private static Map<String, BeanDependency> resolveDependencies(List<String> dependencies, Class<?> genericType) {
        return dependencies.stream()
                .collect(toMap(identity(), name -> new BeanDependency(name, genericType, false)));
    }
}
