package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanDefinitionConstructionException;
import com.bobocode.hoverla.bring.exception.BeanDependencyInjectionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

/**
 * Java configuration-based implementation of {@link BeanDefinition}.
 *
 * <p>Requires instance of a class marked with {@link Configuration @Configuration} annotation
 * and method marked with {@link Bean @Bean} annotation that represents this bean initialization point.</p>
 *
 * @see Bean @Bean
 * @see Configuration @Configuration
 * @see AbstractBeanDefinition
 * @see BeanDefinition
 */
@Slf4j
public class ConfigBasedBeanDefinition extends AbstractBeanDefinition {

    private final Object configInstance;

    private final Method beanMethod;

    /**
     * During execution doesn't instantiate target bean,
     * but parses all info such as name, type, dependencies etc. and preserves this info to be used later on.
     *
     * @param configInstance instance of a class marked as {@link Configuration} that contains method representing target bean
     * @param beanMethod     method that represents target bean and is used to instantiate target bean
     * @throws BeanDefinitionConstructionException when config class of the instance passed is not marked as {@link Configuration}
     *                                             or bean method is not marked as {@link Bean}
     * @throws NullPointerException                when any of arguments passed is null
     */
    public ConfigBasedBeanDefinition(Object configInstance, Method beanMethod) {
        Objects.requireNonNull(configInstance, "Configuration class instance is null");
        Objects.requireNonNull(beanMethod, "Configuration bean method to create bean is null");

        log.debug("Creating {} from method '{}'", ConfigBasedBeanDefinition.class.getSimpleName(), beanMethod);
        this.configInstance = configInstance;
        this.beanMethod = beanMethod;

        this.name = resolveName(beanMethod);
        log.trace("Bean name is '{}'", name);

        this.type = getType(beanMethod);
        log.trace("'{}' bean type is '{}'", name, type);

        this.dependencies = resolveDependencies(beanMethod);
        log.trace("'{}' bean dependencies are {}", name, dependencies);
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
     * See {@link BeanDefinition#isPrimary()}.
     *
     * @return value of {@link Bean#primary()} property of {@link Bean @Bean} annotation
     * on this {@link ConfigBasedBeanDefinition#beanMethod}
     */
    @Override
    public boolean isPrimary() {
        return beanMethod.getAnnotation(Bean.class).primary();
    }

    /**
     * Resolves name of current {@link BeanDefinition}.
     *
     * <p>This name will be either equal to target {@link Method#getName()} or to name specified
     * in {@link Bean @Bean} annotation on this {@link Method}.</p>
     *
     * @param beanMethod {@link Method} annotated with {@link Bean @Bean}
     * @return name of current {@link BeanDefinition}.
     */
    private String resolveName(Method beanMethod) {
        Bean annotation = beanMethod.getAnnotation(Bean.class);
        String beanName = annotation.value();
        if (beanName.isEmpty()) {
            return beanMethod.getName();
        }
        return beanName;
    }

    /**
     * Resolves type of current {@link BeanDefinition}.
     *
     * <p>This type equals to return type of target {@link Method} annotated with {@link Bean @Bean}.</p>
     *
     * @param beanMethod {@link Method} annotated with {@link Bean @Bean}
     * @return type of current {@link BeanDefinition}.
     */
    private Class<?> getType(Method beanMethod) {
        return beanMethod.getReturnType();
    }

    /**
     * Resolves dependencies of current {@link BeanDefinition} and store them in a {@link Map}.
     *
     * <p>Keys are taken from parameter names of target {@link Method} annotated with {@link Bean @Bean}.</p>
     * <p>Values are taken from parameter types of target {@link Method} annotated with {@link Bean @Bean}.</p>
     *
     * @param beanMethod {@link Method} annotated with {@link Bean @Bean}
     * @return {@link Map} of names and types of dependent {@link BeanDefinition}s.
     */
    private Map<String, Class<?>> resolveDependencies(Method beanMethod) {
        return Arrays.stream(beanMethod.getParameters())
                .collect(toMap(this::resolveParameterName, Parameter::getType));
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            log.debug("Instantiating bean with name '{}'", name);

            Map<String, Class<?>> dependenciesMap = Arrays.stream(dependencies)
                    .collect(toMap(BeanDefinition::name, BeanDefinition::type));
            log.trace("Dependencies received are {}", dependenciesMap);

            Object createdInstance = doCreateInstance(Lists.newArrayList(dependencies));
            log.debug("Bean with name '{}' was instantiated", name);
            return createdInstance;
        } catch (Exception e) {
            throw new BeanInstanceCreationException("Bean with name '%s' can't be instantiated".formatted(name), e);
        }
    }

    private Object doCreateInstance(List<BeanDefinition> dependencies) throws InvocationTargetException, IllegalAccessException {
        Parameter[] parameters = beanMethod.getParameters();
        Object[] constructorArguments = new Object[parameters.length];

        resolveQualifiedDependencies(dependencies, parameters, constructorArguments);
        resolveUnqualifiedDependencies(dependencies, parameters, constructorArguments);
        return beanMethod.invoke(configInstance, constructorArguments);
    }


    private void resolveUnqualifiedDependencies(List<BeanDefinition> dependencies, Parameter[] parameters,
                                                Object[] constructorArgs) {
        Map<Integer, Parameter> nonQualifiedParams = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (!parameter.isAnnotationPresent(Qualifier.class)) {
                nonQualifiedParams.put(i, parameter);
            }
        }
        nonQualifiedParams
                .entrySet()
                .stream()
                .sorted(ConfigBasedBeanDefinition::subclassesFirst)
                .forEachOrdered(entry -> resolveArgumentFromEntry(entry, dependencies, constructorArgs));

    }

    private void resolveQualifiedDependencies(List<BeanDefinition> dependencies, Parameter[] parameters,
                                              Object[] constructorArgs) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Qualifier.class)) {
                constructorArgs[i] = getBeanDefinitionByParameter(parameter, dependencies).getInstance();
            }
        }
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
        Object matchingArgument = getBeanDefinitionByParameter(parameter, dependencies).getInstance();
        constructorArgs[index] = matchingArgument;
    }

    private BeanDefinition getBeanDefinitionByParameter(Parameter parameter, List<BeanDefinition> dependencies) {
        BeanDefinition beanDefinition = dependencies.stream()
                .filter(bd -> parameter.getType().isAssignableFrom(bd.type()))
                .filter(bd -> checkNamesMatch(parameter, bd))
                .findFirst()
                .orElseThrow(() -> new BeanDependencyInjectionException(
                        "'%s' bean has no dependency that matches parameter '%s'".formatted(name, parameter.getType().getName())));

        dependencies.remove(beanDefinition); // remove mapped dependency to avoid conflicts
        return beanDefinition;
    }

    private boolean checkNamesMatch(Parameter parameter, BeanDefinition bd) {
        if (parameter.isAnnotationPresent(Qualifier.class)) {
            String qualifierName = parameter.getAnnotation(Qualifier.class).value();
            return bd.name().equals(qualifierName);
        }
        return true;
    }

    private String resolveParameterName(Parameter parameter) {
        if (parameter.isAnnotationPresent(Qualifier.class)) {
            return parameter.getAnnotation(Qualifier.class).value();
        }
        return parameter.getType().getName();
    }
}
