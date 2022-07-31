package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanDefinitionConstructionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Java class-based implementation of {@link BeanDefinition}.
 *
 * <p>Requires instance of class marked with {@link Bean @Bean} annotation.</p>
 *
 * @see Bean
 * @see AbstractBeanDefinition
 * @see BeanDefinition
 */
@Slf4j
public class ClassBasedBeanDefinition extends AbstractBeanDefinition {

    private static final String INSTANTIATION_EXCEPTION_MESSAGE = "'%s' bean can't be instantiated";

    private Constructor<?> constructor;

    private List<Field> injectionFields;

    /**
     * During execution doesn't instantiate target bean,
     * but parses all info such as name, type, dependencies. and preserves this info to be used later on.
     *
     * @param beanClass instance of a class marked with {@link Bean @Bean}.
     * @throws BeanDefinitionConstructionException when an unexpected exception occurred during bean instantiation
     * @throws NullPointerException                when passed {@code beanClass} is null
     */
    public ClassBasedBeanDefinition(Class<?> beanClass) {
        Objects.requireNonNull(beanClass, "Bean class cannot be null");

        this.type = beanClass;
        log.debug("Creating {} from class '{}'", ClassBasedBeanDefinition.class.getSimpleName(), this.type.getName());

        this.name = resolveName(beanClass);
        log.trace("Resolved name is '{}'", name);

        this.dependencies = resolveDependencies(beanClass);
        log.trace("Resolved dependencies are {}", dependencies);
    }

    /**
     * See {@link BeanDefinition#instantiate(BeanDefinition...)}
     *
     * @param dependencies {@link BeanDefinition} objects that are treated as required dependencies for
     *                     instantiating a bean from current {@link BeanDefinition}
     */
    @Override
    public void instantiate(BeanDefinition... dependencies) {
        if (!isInstantiated()) {
            log.debug("Creating new instance of bean '{}'", name);
            instance = createInstance(dependencies);
        }
    }

    /**
     * Resolves name of current {@link BeanDefinition}.
     *
     * <p>This name will be either equal to current bean {@link Class#getName()} or to
     * name specified in {@link Bean} annotation.</p>
     *
     * @param beanClass instance of a class marked with {@link Bean @Bean}.
     * @return name of current {@link BeanDefinition}.
     */
    private String resolveName(Class<?> beanClass) {
        Bean annotation = beanClass.getAnnotation(Bean.class);
        String beanName = annotation.name();
        if (isBlank(beanName)) {
            return beanClass.getName();
        }
        return beanName;
    }

    /**
     * Resolves dependencies of current {@link BeanDefinition} and store them in a {@link Map}.
     *
     * <p>Keys are taken from {@link Qualifier @Qualifier} annotation on dependency constructor {@link Parameter}
     * or {@link Field} annotated with {@link Inject @Inject}.</p>
     *
     * <p>In case annotation is not present key will be equal to {@link Class#getName()} of a corresponding dependency.</p>
     *
     * <p>Values are taken from {@link Parameter#getType()} or {@link Field#getType()} of a corresponding dependency.</p>
     *
     * @return {@link Map} of names and types of dependent {@link BeanDefinition}s.
     */
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

    private <M extends AnnotatedElement> String resolveMemberName(M classMember, Class<?> memberType) {
        return Optional.ofNullable(classMember.getAnnotation(Qualifier.class))
                .map(Qualifier::value)
                .orElse(memberType.getName());
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            Objects.requireNonNull(dependencies);
            log.debug("Initializing '{}' bean instance", name);
            this.instance = doCreateInstance(dependencies);
            log.debug("'{}' bean was instantiated", name);
            return instance;
        } catch (Exception e) { // all @SneakyThrows stuff is caught here
            throw new BeanInstanceCreationException(INSTANTIATION_EXCEPTION_MESSAGE.formatted(name), e);
        }
    }

    @SneakyThrows
    private Object doCreateInstance(BeanDefinition... dependencies) {
        Object beanInstance = createInstanceUsingConstructor(dependencies);
        injectionFields.forEach(field -> injectFieldDependencies(beanInstance, field, dependencies));
        return beanInstance;
    }

    @SneakyThrows
    private Object createInstanceUsingConstructor(BeanDefinition... dependencies) {
        if (constructor.getParameterCount() == 0) {
            return constructor.newInstance();
        }
        Object[] dependenciesForConstructor = Arrays.stream(constructor.getParameters())
                .map(parameter -> getMatchingDependency(parameter, dependencies))
                .filter(Optional::isPresent)
                .map(dependency -> dependency.get().getInstance())
                .toArray();

        return constructor.newInstance(dependenciesForConstructor);
    }

    private Optional<BeanDefinition> getMatchingDependency(Parameter parameter, BeanDefinition[] dependencies) {
        return Arrays.stream(dependencies)
                .filter(dependency -> checkDependenciesMatch(parameter, parameter.getType(), dependency))
                .findAny();
    }

    private void injectFieldDependencies(Object beanInstance, Field targetField, BeanDefinition... dependencies) {
        Arrays.stream(dependencies)
                .filter(dependency -> checkDependenciesMatch(targetField, targetField.getType(), dependency))
                .findAny()
                .ifPresent(dependency -> setFieldValue(targetField, beanInstance, dependency));
    }

    private <T extends AnnotatedElement> boolean checkDependenciesMatch(T targetDependency, Class<?> targetType,
                                                                        BeanDefinition sourceDependency) {
        boolean typesAssignable = targetType.isAssignableFrom(sourceDependency.type());
        if (!typesAssignable) {
            return false;
        }

        if (targetDependency.isAnnotationPresent(Qualifier.class)) {
            String qualifierValue = targetDependency.getAnnotation(Qualifier.class).value();
            return qualifierValue.equals(sourceDependency.name());
        }

        String targetTypeName = targetType.getName();
        return targetTypeName.equals(sourceDependency.name());
    }

    @SneakyThrows
    @SuppressWarnings("java:S3011")
    private void setFieldValue(Field field, Object beanInstance, BeanDefinition dependency) {
        field.setAccessible(true);
        field.set(beanInstance, dependency.getInstance());
    }
}
