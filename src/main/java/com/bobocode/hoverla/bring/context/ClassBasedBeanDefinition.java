package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Java Class-based bean definition. Requires instance of class marked with {@link Bean} instance.
 * Field should be marked with {@link Inject} annotation for dependencies injection.
 */
@Slf4j
public class ClassBasedBeanDefinition implements BeanDefinition {

    private static final String INSTANTIATION_EXCEPTION_MESSAGE = "'%s' bean can't be instantiated";

    private Object instance;

    private final String name;

    private final Class<?> type;

    private final Map<String, Class<?>> dependencies;

    public ClassBasedBeanDefinition(Class<?> beanClass) {
        this.type = beanClass;
        log.debug("Creating {} from class '{}'", ClassBasedBeanDefinition.class.getSimpleName(), this.type.getName());

        this.name = resolveName(beanClass);
        String beanDefinitionClassName = this.type.getSimpleName();
        log.trace("{} - resolved name is '{}'", beanDefinitionClassName, name);

        //todo pretty print map
        this.dependencies = resolveDependencies(beanClass);
        log.trace("'{}' - resolved dependencies are {}", beanDefinitionClassName, dependencies);
    }

    /**
     * @return name of bean which is either equals to bean class name
     * or to name specified in {@link Bean} or {@link Qualifier} annotations
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @return return type of bean
     */
    @Override
    public Class<?> type() {
        return type;
    }

    /**
     * @return dependencies map where keys are bean constructor parameters name or bean fields name, and
     * values are parameters or fields types
     */
    @Override
    public Map<String, Class<?>> dependencies() {
        return dependencies;
    }

    /**
     * Lazily initializes bean on the first call. After it happened further calls return a cached instance.
     */
    @Override
    public Object instance(BeanDefinition... dependencies) {
        if (Objects.isNull(instance)) {
            return createInstance(dependencies);
        }
        log.debug("Getting existing '{}' bean instance", name);
        return instance;
    }

    private static String resolveName(Class<?> beanClass) {
        Bean annotation = beanClass.getAnnotation(Bean.class);
        String beanName = annotation.name();
        if (isBlank(beanName)) {
            return uncapitalize(beanClass.getSimpleName());
        }
        return beanName;
    }

    private Map<String, Class<?>> resolveDependencies(Class<?> beanClass) {
        Map<String, Class<?>> resolvedDependencies = new LinkedHashMap<>();
        resolvedDependencies.putAll(resolveConstructorDependencies(beanClass));
        resolvedDependencies.putAll(resolveFieldDependencies(beanClass));

        return resolvedDependencies;
    }

    private Map<String, Class<?>> resolveConstructorDependencies(Class<?> beanClass) {
        // getting first element only since constructors were already validated
        // see BeanAnnotationClassValidator.java
        Constructor<?> beanConstructor = beanClass.getConstructors()[0];
        Parameter[] constructorParameters = beanConstructor.getParameters();

        if (!isEmpty(constructorParameters)) {
            return stream(constructorParameters)
                    .collect(toMap(param -> resolveMemberName(param).orElse(param.getName()), Parameter::getType));
        }
        return emptyMap();
    }

    private Map<String, Class<?>> resolveFieldDependencies(Class<?> beanClass) {
        List<Field> declaredInjectionFields = stream(beanClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .toList();

        if (!declaredInjectionFields.isEmpty()) {
            return declaredInjectionFields.stream()
                    .collect(toMap(field -> resolveMemberName(field).orElse(field.getName()), Field::getType));
        }
        return emptyMap();
    }

    private <M extends AnnotatedElement> Optional<String> resolveMemberName(M classMember) {
        //who validates Qualifier value?
        return Optional.ofNullable(classMember.getAnnotation(Qualifier.class))
                .map(Qualifier::value);
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            Objects.requireNonNull(dependencies);
            log.debug("Initializing '{}' bean instance", name);
            instance = doCreateInstance(dependencies);
            log.debug("'{}' bean was instantiated", name);
            return instance;
        } catch (Exception e) {
            throw new BeanInstanceCreationException(INSTANTIATION_EXCEPTION_MESSAGE.formatted(name), e);
        }
    }

    private Object doCreateInstance(BeanDefinition... dependencies) {
        Optional<Constructor<?>> constructor = stream(type.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class)).findFirst();

        List<Field> fieldsWithInjectAnnotation = stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .toList();

        Object beanInstance;

        try {
            if (constructor.isPresent()) {
                beanInstance = createInstanceUsingConstructor(constructor.get(), dependencies);
            } else
                beanInstance = type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BeanInstanceCreationException(INSTANTIATION_EXCEPTION_MESSAGE.formatted(name), e);
        }


        List<BeanDefinition> dependenciesForFields = fieldsWithInjectAnnotation.stream()
                .flatMap(field -> stream(dependencies)
                        .filter(dependency -> equalBeanName(field, dependency)))
                .toList();

        fieldsWithInjectAnnotation.forEach(field -> injectDependenciesIntoFields(beanInstance, dependenciesForFields, field));

        return beanInstance;
    }

    private boolean equalBeanName(Field field, BeanDefinition dependency) {
        if (field.isAnnotationPresent(Qualifier.class) &&
                field.getAnnotation(Qualifier.class).value().equals(dependency.name())) {
            return true;
        }
        final Class<?> fieldType = field.getType();
        return fieldType.getName().equals(dependency.name()) &&
                fieldType.equals(dependency.type());
    }

    private static Object createInstanceUsingConstructor(Constructor<?> constructor, BeanDefinition... dependencies) {
        List<Object> dependenciesForConstructor = stream(constructor.getParameters())
                .map(parameter -> getFromDependencies(parameter, dependencies))
                .filter(Optional::isPresent)
                .map(dependency -> dependency.get().instance())
                .toList();

        try {
            return constructor.newInstance(dependenciesForConstructor.toArray());
        } catch (Exception exception) {
            throw new BeanInstanceCreationException("Invalid bean dependencies. Bean %s. Dependencies: ".formatted(constructor.getDeclaringClass())
                    + Arrays.toString(dependencies), exception);
        }
    }

    private static Optional<BeanDefinition> getFromDependencies(Parameter parameter, BeanDefinition[] dependencies) {
        return stream(dependencies)
                .filter(dependency -> isParameterEqualWithDependency(parameter, dependency))
                .findAny();
    }

    private static boolean isParameterEqualWithDependency(Parameter parameter, BeanDefinition dependency) {
        final boolean typesEqual = dependency.type().equals(parameter.getType());

        if (typesEqual) {
            if (parameter.isAnnotationPresent(Qualifier.class)) {
                return parameter.getAnnotation(Qualifier.class).value().equals(
                        dependency.name());
            }
            return parameter.getType().getName().equals(
                    dependency.name());
        }
        return false;
    }

    @SuppressWarnings("java:S3011")
    private void injectDependenciesIntoFields(Object beanInstance, List<BeanDefinition> dependenciesForFields, Field field) {
        try {
            Optional<BeanDefinition> beanDefinition = dependenciesForFields.stream()
                    .filter(dependency -> {
                        if (field.isAnnotationPresent(Qualifier.class) &&
                                field.getAnnotation(Qualifier.class).value().equals(dependency.name())) {
                            return true;
                        }
                        return field.getType().getName().equals(dependency.name());
                    })
                    .findAny();
            if (beanDefinition.isPresent()) {
                field.setAccessible(true);
                field.set(beanInstance, Objects.requireNonNull(beanDefinition.get().instance()));
            }
        } catch (IllegalAccessException e) {
            throw new BeanInstanceCreationException(INSTANTIATION_EXCEPTION_MESSAGE.formatted(name), e);
        }
    }
}
