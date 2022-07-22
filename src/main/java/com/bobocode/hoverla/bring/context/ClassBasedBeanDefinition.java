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
        long countOfConstructors = Arrays.stream(beanClass.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .count();

        if (countOfConstructors > 1) {
            throw new BeanDefinitionConstructionException("'class %s' bean has multiple constructors marked as @%s".formatted(beanClass.getName(), Inject.class.getSimpleName()));
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
        Map<String, Class<?>> dependencies = new HashMap<>();

        if (beanClass.getConstructors().length != 0) {
            var constructors = Arrays.stream(beanClass.getConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                    .map(Constructor::getParameters)
                    .flatMap(Arrays::stream)
                    .collect(toMap(Parameter::getName, Parameter::getType));
            dependencies.putAll(constructors);
        }

        if (beanClass.getFields().length != 0) {
            var qualifierAnnotatedFields = Arrays.stream(beanClass.getFields())
                    .filter(field -> field.isAnnotationPresent(Inject.class) && field.isAnnotationPresent(Qualifier.class))
                    .collect(toMap(field -> field.getAnnotation(Qualifier.class).value(), Field::getType));

            var onlyInjectAnnotatedFields = Arrays.stream(beanClass.getFields())
                    .filter(field -> field.isAnnotationPresent(Inject.class) && !field.isAnnotationPresent(Qualifier.class))
                    .collect(toMap(Field::getName, Field::getType));

            dependencies.putAll(qualifierAnnotatedFields);
            dependencies.putAll(onlyInjectAnnotatedFields);
        }
        return dependencies;
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
            throw new BeanInstanceCreationException("'class %s' bean can't be instantiated".formatted(type.getName()), e);
        }
    }

    private Object getInstance(BeanDefinition... dependencies) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        Optional<Constructor<?>> constructorOptional = Arrays.stream(type.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class)).findAny();

        List<Field> fieldsWithInjectAnnotation = Arrays.stream(type.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Inject.class)).toList();
        //List<Field> fieldsWithQualifierAnnotation = Arrays.stream(type.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Qualifier.class)).toList();

        List<Field> fields = new ArrayList<>();

        if (!fieldsWithInjectAnnotation.isEmpty()) {
            fields.addAll(fieldsWithInjectAnnotation);
        }

//        if (!fieldsWithQualifierAnnotation.isEmpty()) {
//            fields.addAll(fieldsWithQualifierAnnotation);
//        }

        Object newInstance = type.getDeclaredConstructor().newInstance();

        if (constructorOptional.isPresent() && !(fields.isEmpty())) {

            newInstance = createInstanceUsingConstructor(constructorOptional.get(), dependencies);

            if (newInstance != null) {

                List<Field> fieldsForInjectInClass = Arrays.stream(newInstance.getClass().getFields()).filter(field -> field.isAnnotationPresent(Inject.class))
                        .toList();

                List<BeanDefinition> beanDefinitionsForInjectionIntoFields = fieldsForInjectInClass.stream()
                        .flatMap(field -> Arrays.stream(dependencies).filter(dependency -> dependency.name().equals(field.getClass().getSimpleName()) && dependency.type().equals(field.getType())))
                        .toList();

                fieldsForInjectInClass.forEach(field -> {
                    Optional<BeanDefinition> optionalBeanDefinition = beanDefinitionsForInjectionIntoFields.stream()
                            .flatMap(object -> Arrays.stream(dependencies).filter(dependency -> dependency.name().equals(object.name()) && dependency.type().equals(object.type())))
                            .findAny();

                    try {
                        if (optionalBeanDefinition.isPresent()) {
                            BeanDefinition oldValue = optionalBeanDefinition.get().getClass().getDeclaredConstructor().newInstance();
                            field.setAccessible(true);
                            field.set(oldValue, optionalBeanDefinition.get());
                        }
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        if (constructorOptional.isEmpty() && !fields.isEmpty()) {
            Object fieldInstance = type.getDeclaredConstructor().newInstance();

            List<BeanDefinition> dependenciesForFields = fields.stream()
                    .flatMap(field -> Arrays.stream(dependencies).filter(dependency -> dependency.getClass().getSimpleName().equals(field.getClass().getSimpleName())))
                    .toList();

            fields.forEach(field -> {
                try {
                    BeanDefinition beanDefinition = dependenciesForFields.stream().filter(dependency -> dependency.name().equals(field.getName())).findAny().get();
                    field.setAccessible(true);
                    field.set(fieldInstance, beanDefinition);
                } catch (IllegalAccessException e) {
                    throw new BeanInstanceCreationException("", e);
                }
            });
            newInstance = fieldInstance;
        }

        if(constructorOptional.isPresent() && fields.isEmpty()) {
            newInstance = createInstanceUsingConstructor(constructorOptional.get(), dependencies);
        }

        return newInstance;
    }

    private static Object createInstanceUsingConstructor(Constructor<?> constructor, BeanDefinition... dependencies) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object newInstance = constructor.getClass();
        List<Parameter> constructorParameters = Arrays.stream(constructor.getParameters()).toList();

        List<Object> dependenciesForConstructor = constructorParameters.stream()
                .flatMap(parameter -> Arrays.stream(dependencies)
                        .filter(dependency -> dependency.name().equals(parameter.getClass().getSimpleName()) && dependency.type().equals(parameter.getType())))
                .map(BeanDefinition::instance)
                .toList();


        if (constructor.getParameterCount() == dependenciesForConstructor.size()) {
            newInstance = constructor.newInstance(constructorParameters);
        }
        return newInstance;
    }
}
