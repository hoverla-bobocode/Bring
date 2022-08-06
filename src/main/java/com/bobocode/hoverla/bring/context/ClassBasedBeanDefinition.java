package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanDefinitionConstructionException;
import com.bobocode.hoverla.bring.exception.BeanDependencyInjectionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
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
            log.debug("Creating new instance of bean with name '{}'", name);
            instance = createInstance(dependencies);
        }
    }

    /**
     * See {@link BeanDefinition#isPrimary()}.
     *
     * @return value of {@link Bean#primary()} property of {@link Bean @Bean} annotation
     * on this {@link ClassBasedBeanDefinition#type} .
     */
    @Override
    public boolean isPrimary() {
        return type.getAnnotation(Bean.class).primary();
    }

    /**
     * @return {@code false} always since {@link ClassBasedBeanDefinition} can never be a {@link Collection}
     */
    @Override
    public boolean isCollection() {
        return false;
    }

    /**
     * @return {@code null} always since {@link ClassBasedBeanDefinition} can never be a {@link Collection}
     */
    @Override
    @Nullable
    public Class<?> collectionGenericType() {
        return null;
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
        String beanName = annotation.value();
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
    private Map<String, BeanDependency> resolveDependencies(Class<?> beanClass) {
        Map<String, BeanDependency> resolvedDependencies = new HashMap<>();
        resolvedDependencies.putAll(resolveConstructorDependencies(beanClass));
        resolvedDependencies.putAll(resolveFieldDependencies(beanClass));

        return resolvedDependencies;
    }

    private Map<String, BeanDependency> resolveConstructorDependencies(Class<?> beanClass) {
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

    private Map<String, BeanDependency> resolveConstructorDependencies(Constructor<?> beanConstructor) {
        this.constructor = beanConstructor;

        Parameter[] constructorParameters = constructor.getParameters();
        if (isEmpty(constructorParameters)) {
            return emptyMap();
        }
        return Arrays.stream(constructorParameters)
                .map(BeanDependency::fromParameter)
                .collect(toMap(BeanDependency::getName, identity()));
    }

    private Map<String, BeanDependency> resolveFieldDependencies(Class<?> beanClass) {
        List<Field> classFields = Lists.newArrayList(beanClass.getDeclaredFields());
        this.injectionFields = getElementsAnnotatedWith(classFields, Inject.class);

        if (injectionFields.isEmpty()) {
            return emptyMap();
        }
        return injectionFields.stream()
                .map(BeanDependency::fromField)
                .collect(toMap(BeanDependency::getName, identity()));
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            Objects.requireNonNull(dependencies);
            log.debug("Instantiating bean of name '{}' with {} dependencies", name, dependencies.length);
            this.instance = doCreateInstance(dependencies);
            log.debug("Bean with name '{}' was instantiated", name);
            return instance;
        } catch (Exception e) { // all @SneakyThrows stuff is caught here
            throw new BeanInstanceCreationException("Bean with name '%s' can't be instantiated".formatted(name), e);
        }
    }

    @SneakyThrows
    private Object doCreateInstance(BeanDefinition... dependencies) {
        List<BeanDefinition> dependencyList = Lists.newArrayList(dependencies);
        Object beanInstance = createInstanceUsingConstructor(dependencyList);

        doFieldInjection(beanInstance, dependencyList);
        return beanInstance;
    }

    @SneakyThrows
    private Object createInstanceUsingConstructor(List<BeanDefinition> dependencies) {
        if (constructor.getParameterCount() == 0) {
            return constructor.newInstance();
        }
        List<Parameter> parameters = Lists.newArrayList(constructor.getParameters());
        Object[] constructorArgs = new Object[parameters.size()];

        resolveQualifiedParameters(dependencies, parameters, constructorArgs);
        resolveNonQualifiedParameters(dependencies, parameters, constructorArgs);
        return constructor.newInstance(constructorArgs);
    }

    private void resolveQualifiedParameters(List<BeanDefinition> dependencies, List<Parameter> parameters,
                                            Object[] constructorArgs) {
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if (parameter.isAnnotationPresent(Qualifier.class)) {
                constructorArgs[i] = getMatchingParameterDependency(parameter, dependencies).getInstance();
            }
        }
    }

    private void resolveNonQualifiedParameters(List<BeanDefinition> dependencies, List<Parameter> parameters,
                                               Object[] constructorArgs) {
        Map<Integer, Parameter> indexedParameters = new LinkedHashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if (!parameter.isAnnotationPresent(Qualifier.class)) {
                indexedParameters.put(i, parameter);
            }
        }

        indexedParameters.entrySet()
                .stream()
                .sorted(ClassBasedBeanDefinition::subclassesFirst)
                .forEachOrdered(entry -> resolveArgumentFromEntry(entry, dependencies, constructorArgs));

    }

    private static int subclassesFirst(Map.Entry<Integer, Parameter> parameterEntry1,
                                       Map.Entry<Integer, Parameter> parameterEntry2) {
        Class<?> type1 = parameterEntry1.getValue().getType();
        Class<?> type2 = parameterEntry2.getValue().getType();
        if (type1.isAssignableFrom(type2)) {
            return 1;
        }
        return -1;
    }

    private void resolveArgumentFromEntry(Map.Entry<Integer, Parameter> indexedParameter,
                                          List<BeanDefinition> dependencies, Object[] constructorArgs) {
        Integer index = indexedParameter.getKey();
        Parameter parameter = indexedParameter.getValue();
        Object matchingArgument = getMatchingParameterDependency(parameter, dependencies).getInstance();
        constructorArgs[index] = matchingArgument;
    }

    private BeanDefinition getMatchingParameterDependency(Parameter parameter, List<BeanDefinition> dependencies) {
        Optional<BeanDefinition> dependency = dependencies.stream()
                .filter(bd -> parameterMatch(parameter, bd))
                .findAny();
        dependency.ifPresent(dependencies::remove); // remove mapped dependency to avoid conflicts

        return dependency
                .orElseThrow(() -> new BeanDependencyInjectionException(
                        "Unable to resolve injection of constructor parameter '%s'".formatted(parameter.getName())));
    }

    @SuppressWarnings("java:S2589")
    private boolean parameterMatch(Parameter parameter, BeanDefinition dependencyToMatch) {
        Class<?> parameterType = parameter.getType();
        boolean typeMatch = parameterType.isAssignableFrom(dependencyToMatch.type());
        if (!typeMatch) {
            return false;
        }

        Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
        if (Objects.nonNull(qualifier)) {
            return qualifier.value().equals(dependencyToMatch.name());
        }

        boolean parameterIsCollection = Collection.class.isAssignableFrom(parameterType);
        boolean dependencyIsCollection = dependencyToMatch.isCollection();

        if ((parameterIsCollection && !dependencyIsCollection) ||
                (!parameterIsCollection && dependencyIsCollection)) {
            return false;
        }

        if (dependencyIsCollection && parameterIsCollection) {
            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
            Class<?> actualCollectionType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            return actualCollectionType.isAssignableFrom(dependencyToMatch.collectionGenericType());
        }

        return true;
    }

    private void doFieldInjection(Object beanInstance, List<BeanDefinition> dependencies) {
        List<Field> qualifiedFields = getElementsAnnotatedWith(injectionFields, Qualifier.class);
        qualifiedFields.forEach(field -> injectIntoField(beanInstance, field, dependencies));

        // injection of fields without @Qualifier
        ListUtils.removeAll(injectionFields, qualifiedFields)
                .stream()
                .sorted((f1, f2) -> f1.getType().isAssignableFrom(f2.getType()) ? 1 : -1) // superclasses last
                .forEach(field -> injectIntoField(beanInstance, field, dependencies));

        verifyFieldInjection(beanInstance);
    }

    private void injectIntoField(Object beanInstance, Field targetField, List<BeanDefinition> dependencies) {
        dependencies.stream()
                .filter(dependency -> fieldMatch(targetField, dependency))
                .findAny()
                .ifPresent(dependency -> {
                    setFieldValue(targetField, beanInstance, dependency);
                    dependencies.remove(dependency);
                });
    }

    @SuppressWarnings("java:S2589")
    private boolean fieldMatch(Field field, BeanDefinition dependencyToMatch) {
        Class<?> fieldType = field.getType();
        boolean typeMatch = fieldType.isAssignableFrom(dependencyToMatch.type());
        if (!typeMatch) {
            return false;
        }

        Qualifier qualifier = field.getAnnotation(Qualifier.class);
        if (Objects.nonNull(qualifier)) {
            return qualifier.value().equals(dependencyToMatch.name());
        }

        boolean fieldIsCollection = Collection.class.isAssignableFrom(fieldType);
        boolean dependencyIsCollection = dependencyToMatch.isCollection();

        if ((fieldIsCollection && !dependencyIsCollection) ||
                (!fieldIsCollection && dependencyIsCollection)) {
            return false;
        }

        if (dependencyIsCollection && fieldIsCollection) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Class<?> actualCollectionType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            return actualCollectionType.isAssignableFrom(dependencyToMatch.collectionGenericType());
        }
        return true;
    }

    private void verifyFieldInjection(Object beanInstance) {
        List<String> unresolvedFieldNames = injectionFields.stream()
                .filter(field -> injectionFailedForField(field, beanInstance))
                .map(Field::getName)
                .toList();
        if (!unresolvedFieldNames.isEmpty()) {
            log.warn("Found {} unresolved fields of bean instance after injection", unresolvedFieldNames.size());
            throw new BeanDependencyInjectionException(
                    "Field injection failed for bean instance of type %s. Unresolved fields: %s"
                            .formatted(beanInstance.getClass().getName(), unresolvedFieldNames));
        }
    }

    private <T extends AnnotatedElement> List<T> getElementsAnnotatedWith(List<T> elements,
                                                                          Class<? extends Annotation> annotation) {
        return elements.stream()
                .filter(e -> e.isAnnotationPresent(annotation))
                .toList();
    }

    @SneakyThrows
    @SuppressWarnings("java:S3011")
    private void setFieldValue(Field field, Object beanInstance, BeanDefinition dependency) {
        field.setAccessible(true);
        field.set(beanInstance, dependency.getInstance());
    }

    @SneakyThrows
    @SuppressWarnings("java:S3011")
    private boolean injectionFailedForField(Field targetField, Object beanInstance) {
        targetField.setAccessible(true);
        return Objects.isNull(targetField.get(beanInstance));
    }
}
