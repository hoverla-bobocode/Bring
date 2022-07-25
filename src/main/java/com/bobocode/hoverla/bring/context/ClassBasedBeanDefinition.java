package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanDefinitionConstructionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class ClassBasedBeanDefinition implements BeanDefinition {

    private Object instance;

    private final String name;

    private final Class<?> type;

    private final Map<String, Class<?>> dependencies;

    public ClassBasedBeanDefinition(Class<?> beanClass) {
        validate(beanClass);

        this.name = getName(beanClass);
        log.trace("Bean name is '{}'", name);

        this.type = beanClass;
        log.trace("'{}' bean type is '{}'", name, type);

        this.dependencies = getDependencies(beanClass);
        log.trace("'{}' bean dependencies are {}", name, dependencies);
    }

    private static void validate(Class<?> beanClass) {
        log.debug("Validating arguments passed to create ClassBased");

        Objects.requireNonNull(beanClass);
        checkIfClassHasManyConstructor(beanClass);
    }

    private static void checkIfClassHasManyConstructor(Class<?> beanClass) {
        if (beanClass.getConstructors().length > 1) {
            throw new BeanDefinitionConstructionException("'class %s' bean has multiple constructors".formatted(beanClass.getName()));
        }
    }

    private static String getName(Class<?> beanClass) {
        var annotation = beanClass.getAnnotation(Bean.class);
        String beanName = annotation.name();

        if (beanName.isEmpty()) {
            return beanClass.getSimpleName();
        }

        return beanName;
    }

    private static Map<String, Class<?>> getDependencies(Class<?> beanClass) {
        Map<String, Class<?>> dependencies = new LinkedHashMap<>();

        if (beanClass.getConstructors().length != 0) {
            Constructor<?> constructor = beanClass.getConstructors()[0];
            var constructorsParameters = Arrays.stream(constructor.getParameters()).collect(toMap(Parameter::getName, Parameter::getType));

            dependencies.putAll(constructorsParameters);
        }

        if (beanClass.getDeclaredFields().length != 0) {
            var fields = Arrays.stream(beanClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Inject.class))
                    .collect(toMap(ClassBasedBeanDefinition::getParameterName, Field::getType));

            dependencies.putAll(fields);
        }

        return dependencies;
    }

    private static String getParameterName(Field field) {
        if (field.isAnnotationPresent(Qualifier.class)) {
            return field.getAnnotation(Qualifier.class).value();
        }

        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    @Override
    public String name() {
        return name;
    }

    @Override

    public Class<?> type() {
        return type;
    }

    @Override
    public Map<String, Class<?>> dependencies() {
        return dependencies;
    }

    @Override
    public Object instance(BeanDefinition... dependencies) {
        if (instance != null) {
            log.debug("Getting existing '{}' bean instance", name);
            return instance;
        }
        return createInstance(dependencies);
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            Objects.requireNonNull(dependencies);
            log.debug("Initializing '{}' bean instance", name);
            instance = getInstance(dependencies);
            log.debug("'{}' bean was instantiated", name);
            return instance;

        } catch (Exception e) {
            throw new BeanInstanceCreationException("'%s' bean can't be instantiated".formatted(type.getSimpleName()), e);
        }
    }

    private Object getInstance(BeanDefinition... dependencies) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        Optional<Constructor<?>> constructorOptional = Arrays.stream(type.getConstructors()).findFirst();

        List<Field> fieldsWithInjectAnnotation = Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .toList();

        Optional<Object> beanInstance = Optional.empty();

        if (constructorOptional.isPresent()) {
            beanInstance = Optional.of(createInstanceUsingConstructor(constructorOptional.get(), dependencies));
        }

        if (beanInstance.isEmpty()) {
            beanInstance = Optional.of(type.getDeclaredConstructor().newInstance());
        }

        List<BeanDefinition> dependenciesForFields = fieldsWithInjectAnnotation.stream()
                .flatMap(field -> Arrays.stream(dependencies)
                        .filter(dependency -> equalBeanName(field, dependency)))
                .toList();

        final Object injectableBean = beanInstance.get();
        fieldsWithInjectAnnotation.forEach(field -> {
            try {
                Optional<BeanDefinition> beanDefinition = dependenciesForFields.stream()
                        .filter(dependency -> {
                            if (field.isAnnotationPresent(Qualifier.class) &&
                                    field.getAnnotation(Qualifier.class).value().equals(dependency.name())) {
                                return true;
                            }
                            return field.getType().getSimpleName().equals(dependency.name());
                        })
                        .findAny();
                if (beanDefinition.isPresent()) {
                    field.setAccessible(true);
                    field.set(injectableBean, Objects.requireNonNull(beanDefinition.get().instance()));
                }
            } catch (IllegalAccessException e) {
                throw new BeanInstanceCreationException(String.format("'%s' bean can't be instantiated", name), e);
            }
        });
        return beanInstance.get();
    }

    private boolean equalBeanName(Field field, BeanDefinition dependency) {
        if (field.isAnnotationPresent(Qualifier.class) &&
                field.getAnnotation(Qualifier.class).value().equals(dependency.name())) {
            return true;
        }
        final Class<?> fieldType = field.getType();
        return fieldType.getSimpleName().equals(dependency.name()) &&
                fieldType.equals(dependency.type());
    }

    private static Object createInstanceUsingConstructor(Constructor<?> constructor, BeanDefinition... dependencies) {
        List<Object> dependenciesForConstructor = Arrays.stream(constructor.getParameters())
                .map(parameter -> getFromDependencies(parameter, dependencies))
                .filter(Optional::isPresent)
                .map(dependency -> dependency.get().instance())
                .toList();

        try {
            return constructor.newInstance(dependenciesForConstructor.toArray());
        } catch (InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException exception) {
            throw new BeanInstanceCreationException("Invalid bean dependencies. Bean %s. Dependencies: ".formatted(constructor.getDeclaringClass())
                    + Arrays.toString(dependencies), exception);
        }
    }

    private static Optional<BeanDefinition> getFromDependencies(Parameter parameter, BeanDefinition[] dependencies) {
        return Arrays.stream(dependencies)
                .filter(dependency -> {
                    final boolean typesEqual = dependency.type().equals(parameter.getType());

                    if (typesEqual) {
                        if (parameter.isAnnotationPresent(Qualifier.class)) {
                            return parameter.getAnnotation(Qualifier.class).value().equals(
                                    dependency.name());
                        }
                        return parameter.getType().getSimpleName().equals(
                                dependency.name());
                    }
                    return false;
                })
                .findAny();
    }
}
