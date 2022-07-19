package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

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

    private Constructor<?> constructor;

    private List<Field> injectionFields;

    public ClassBasedBeanDefinition(Class<?> beanClass) {
        this.type = beanClass;
        log.debug("Creating {} from class '{}'", ClassBasedBeanDefinition.class.getSimpleName(), this.type.getName());

        this.name = resolveName(beanClass);
        log.trace("Resolved name is '{}'", name);

        this.dependencies = resolveDependencies(beanClass);
        log.trace("Resolved dependencies are {}", dependencies);
    }

    /**
     * @return name of bean which is either equals to bean class name
     * or to name specified in {@link Bean}
     */
    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    /**
     * @return dependencies map where keys are bean constructor parameters names taken from type or {@link Qualifier},
     * and bean fields names taken from type or {@link Qualifier}, and
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
            return beanClass.getName();
        }
        return beanName;
    }

    private Map<String, Class<?>> resolveDependencies(Class<?> beanClass) {
        Map<String, Class<?>> resolvedDependencies = new HashMap<>();
        resolvedDependencies.putAll(resolveConstructorDependencies(beanClass));
        resolvedDependencies.putAll(resolveFieldDependencies(beanClass));

        return resolvedDependencies;
    }

    private Map<String, Class<?>> resolveConstructorDependencies(Class<?> beanClass) {
        Constructor<?>[] beanConstructors = beanClass.getConstructors();

        // we assume that at this point we should have only 1 @Inject constructor
        // see BeanAnnotationClassValidator.java
        Optional<Constructor<?>> injectionConstructor = Arrays.stream(beanConstructors)
                .filter(ctor -> ctor.isAnnotationPresent(Inject.class))
                .findFirst();
        if (injectionConstructor.isPresent()) {
            return resolveConstructorDependencies(injectionConstructor.get());
        }

        // we also assume that we should have only 1 plain constructor if @Inject constructor is missing
        Constructor<?> plainConstructor = beanConstructors[0];
        return resolveConstructorDependencies(plainConstructor);
    }

    private Map<String, Class<?>> resolveConstructorDependencies(Constructor<?> beanConstructor) {
        this.constructor = beanConstructor;

        Parameter[] constructorParameters = constructor.getParameters();
        if (isEmpty(constructorParameters)) {
            return emptyMap();
        }
        return Arrays.stream(constructorParameters)
                .collect(toMap(param -> resolveMemberName(param, param.getType()), Parameter::getType));
    }

    private Map<String, Class<?>> resolveFieldDependencies(Class<?> beanClass) {
        this.injectionFields = Arrays.stream(beanClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .toList();

        if (injectionFields.isEmpty()) {
            return emptyMap();
        }
        return injectionFields.stream()
                .collect(toMap(field -> resolveMemberName(field, field.getType()), Field::getType));
    }

    private <M extends AnnotatedElement> String resolveMemberName(M classMember, Class<?> type) {
        return Optional.ofNullable(classMember.getAnnotation(Qualifier.class))
                .map(Qualifier::value)
                .orElse(type.getName());
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            Objects.requireNonNull(dependencies);
            log.debug("Initializing '{}' bean instance", name);
            this.instance = doCreateInstance(dependencies);
            log.debug("'{}' bean was instantiated", name);
            return instance;
        } catch (Exception e) {
            throw new BeanInstanceCreationException(INSTANTIATION_EXCEPTION_MESSAGE.formatted(name), e);
        }
    }

    private Object doCreateInstance(BeanDefinition... dependencies) {
        try {
            Object beanInstance = createInstanceUsingConstructor(dependencies);
            injectionFields.forEach(field -> injectFieldDependencies(beanInstance, field, dependencies));
            return beanInstance;
        } catch (Exception e) {
            throw new BeanInstanceCreationException(INSTANTIATION_EXCEPTION_MESSAGE.formatted(name), e);
        }
    }

    private Object createInstanceUsingConstructor(BeanDefinition... dependencies) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (constructor.getParameterCount() == 0) {
            return constructor.newInstance();
        }
        List<Object> dependenciesForConstructor = Arrays.stream(constructor.getParameters())
                .map(parameter -> getFromDependencies(parameter, dependencies))
                .filter(Optional::isPresent)
                .map(dependency -> dependency.get().instance())
                .toList();

        return constructor.newInstance(dependenciesForConstructor.toArray());
    }

    private void injectFieldDependencies(Object beanInstance, Field targetField, BeanDefinition... dependencies) {
        Arrays.stream(dependencies)
                .filter(dependency -> fieldMatchesDependency(targetField, dependency))
                .findAny()
                .ifPresent(dependency -> setFieldValue(targetField, beanInstance, dependency));
    }

    @SneakyThrows
    @SuppressWarnings("java:S3011")
    private void setFieldValue(Field field, Object beanInstance, BeanDefinition dependency) {
        field.setAccessible(true);
        field.set(beanInstance, dependency.instance());
    }

    private boolean fieldMatchesDependency(Field field, BeanDefinition dependency) {
        if (field.isAnnotationPresent(Qualifier.class)
                && field.getAnnotation(Qualifier.class).value().equals(dependency.name())) {
            return true;
        }
        Class<?> fieldType = field.getType();
        return fieldType.getName().equals(dependency.name()) && fieldType.equals(dependency.type());
    }

    private static Optional<BeanDefinition> getFromDependencies(Parameter parameter, BeanDefinition[] dependencies) {
        return Arrays.stream(dependencies)
                .filter(dependency -> isParameterEqualWithDependency(parameter, dependency))
                .findAny();
    }

    private static boolean isParameterEqualWithDependency(Parameter parameter, BeanDefinition dependency) {
        boolean typesEqual = dependency.type().equals(parameter.getType());

        if (!typesEqual) {
            return false;
        }
        if (parameter.isAnnotationPresent(Qualifier.class)) {
            String qualifierName = parameter.getAnnotation(Qualifier.class).value();
            return qualifierName.equals(dependency.name());
        }
        String parameterTypeName = parameter.getType().getName();
        return parameterTypeName.equals(dependency.name());
    }
}
