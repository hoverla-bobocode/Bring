package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanDefinitionConstructionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

/**
 * Java Configuration-based bean definition. Requires instance of class marked with {@link Configuration} instance
 * and method withing it marked with {@link Bean} that represents this bean initialization point.
 */
@Slf4j
public class ConfigBasedBeanDefinition implements BeanDefinition {

    private final Object configInstance;
    private final Method beanMethod;
    private final String name;
    private final Class<?> type;
    private final Map<String, Class<?>> dependencies;
    private Object instance;

    /**
     * During execution doesn't instantiate target bean,
     * but parses all info such as name, type, dependencies etc. and preserves this info to be used
     *
     * @param configInstance instance of a class marked as {@link Configuration} that contains method representing target bean
     * @param beanMethod     method that represents target bean and is used to instantiate target bean
     * @throws BeanDefinitionConstructionException when config class of the instance passed is not marked as {@link Configuration}
     *                                             or bean method is not marked as {@link Bean}
     * @throws NullPointerException                when any of arguments passed is null
     */
    public ConfigBasedBeanDefinition(Object configInstance, Method beanMethod) {
        validate(configInstance, beanMethod);

        log.debug("Creating {} from method '{}'", ConfigBasedBeanDefinition.class.getSimpleName(), beanMethod);
        this.configInstance = configInstance;
        this.beanMethod = beanMethod;

        this.name = getName(beanMethod);
        log.trace("Bean name is '{}'", name);

        this.type = getType(beanMethod);
        log.trace("'{}' bean type is '{}'", name, type);

        this.dependencies = getDependencies(beanMethod);
        log.trace("'{}' bean dependencies are {}", name, dependencies);
    }

    private static void validate(Object configInstance, Method method) {
        log.debug("Validating arguments passed to create ConfigBased");
        Objects.requireNonNull(configInstance, "Configuration class instance is null");
        Objects.requireNonNull(method, "Configuration method to create bean is null");

        checkClassMarkedAsConfig(configInstance);
        checkMethodMarkedAsBean(method);
    }

    private static void checkClassMarkedAsConfig(Object configInstance) {
        if (!configInstance.getClass().isAnnotationPresent(Configuration.class)) {
            throw new BeanDefinitionConstructionException(
                    "Configuration class instance passed is not marked as @%s".formatted(Configuration.class.getSimpleName())
            );
        }
    }

    private static void checkMethodMarkedAsBean(Method method) {
        if (!method.isAnnotationPresent(Bean.class)) {
            throw new BeanDefinitionConstructionException(
                    "Configuration method to create bean is not marked as @%s".formatted(Bean.class.getSimpleName())
            );
        }
    }

    private static String getName(Method beanMethod) {
        Bean annotation = beanMethod.getAnnotation(Bean.class);
        String beanName = annotation.name();
        if (beanName.isEmpty()) {
            return beanMethod.getName();
        }
        return beanName;
    }

    private static Class<?> getType(Method beanMethod) {
        return beanMethod.getReturnType();
    }

    private static Map<String, Class<?>> getDependencies(Method beanMethod) {
        return Arrays.stream(beanMethod.getParameters())
                     .collect(toMap(ConfigBasedBeanDefinition::getParameterName, Parameter::getType));
    }

    private static String getParameterName(Parameter parameter) {
        if (parameter.isAnnotationPresent(Qualifier.class)) {
            return parameter.getAnnotation(Qualifier.class).value();
        }
        return parameter.getName();
    }

    /**
     * @return name of bean which is either equals to target bean method name
     * or to name specified in {@link Bean} annotation
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @return target bean method return type
     */
    @Override
    public Class<?> type() {
        return type;
    }

    /**
     * @return dependencies map where keys are target bean method parameters name, and
     * values are parameters types
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
        if (instance != null) {
            log.debug("Getting existing '{}' bean instance", name);
            return instance;
        }
        return createInstance(dependencies);
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            log.debug("Initializing '{}' bean instance", name);
            instance = getInstance(dependencies);
            log.debug("'{}' bean was instantiated", name);
            return instance;

        } catch (Exception e) {
            throw new BeanInstanceCreationException("'%s' bean can't be instantiated".formatted(name), e);
        }
    }

    private Object getInstance(BeanDefinition... dependencies) throws InvocationTargetException, IllegalAccessException {
        Object[] args = Arrays.stream(dependencies)
                              .map(BeanDefinition::instance)
                              .toArray();
        return beanMethod.invoke(configInstance, args);
    }
}
