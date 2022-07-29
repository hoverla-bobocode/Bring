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
 * Java configuration-based implementation of {@link BeanDefinition}.
 *
 * <p>Requires instance of a class marked with {@link Configuration @Configuration} annotation
 * and method marked with {@link Bean @Bean} annotation that represents this bean initialization point.</p>
 *
 * @see Bean @Bean
 * @see Configuration @Configuration
 * @see BeanDefinition
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
     * but parses all info such as name, type, dependencies etc. and preserves this info to be used later on.
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

        this.name = resolveName(beanMethod);
        log.trace("Bean name is '{}'", name);

        this.type = getType(beanMethod);
        log.trace("'{}' bean type is '{}'", name, type);

        this.dependencies = resolveDependencies(beanMethod);
        log.trace("'{}' bean dependencies are {}", name, dependencies);
    }

    private void validate(Object configInstance, Method method) {
        log.debug("Validating arguments passed to create ConfigBased");
        Objects.requireNonNull(configInstance, "Configuration class instance is null");
        Objects.requireNonNull(method, "Configuration method to create bean is null");
    }

    /**
     * See {@link BeanDefinition#name()}.
     * <p>This name is either equal to target bean method name or to name specified in {@link Bean} annotation.</p>
     *
     * @return name of current {@link BeanDefinition}.
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * See {@link BeanDefinition#type()}
     * <p>Type is taken from target bean method return type.</p>
     *
     * @return type of current {@link BeanDefinition}.
     */
    @Override
    public Class<?> type() {
        return type;
    }

    /**
     * See {@link BeanDefinition#dependencies()}.
     * <p>Keys are taken from target bean method parameter names.</p>
     * <p>Values are taken from target bean method parameter types.</p>
     *
     * @return map of names and types of dependent {@link BeanDefinition}s.
     */
    @Override
    public Map<String, Class<?>> dependencies() {
        return dependencies;
    }

    /**
     * See {@link BeanDefinition#isInstantiated()}
     *
     * @return {@code true} if current {@link ConfigBasedBeanDefinition#instance} is not null, {@code false} otherwise.
     */
    @Override
    public boolean isInstantiated() {
        return Objects.nonNull(instance);
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
     * See {@link BeanDefinition#getInstance()}.
     *
     * @return bean instance of current {@link BeanDefinition}.
     * @throws NullPointerException when {@link ConfigBasedBeanDefinition#instance} is null.
     */
    @Override
    public Object getInstance() {
        return Objects.requireNonNull(instance, "Instance of %s has not been created yet".formatted(name));
    }

    private String resolveName(Method beanMethod) {
        Bean annotation = beanMethod.getAnnotation(Bean.class);
        String beanName = annotation.name();
        if (beanName.isEmpty()) {
            return beanMethod.getName();
        }
        return beanName;
    }

    private Class<?> getType(Method beanMethod) {
        return beanMethod.getReturnType();
    }

    private Map<String, Class<?>> resolveDependencies(Method beanMethod) {
        return Arrays.stream(beanMethod.getParameters())
                .collect(toMap(this::resolveParameterName, Parameter::getType));
    }

    private Object createInstance(BeanDefinition... dependencies) {
        try {
            Object createdInstance = doCreateInstance(dependencies);
            log.debug("'{}' bean was instantiated", name);
            return createdInstance;
        } catch (Exception e) {
            throw new BeanInstanceCreationException("'%s' bean can't be instantiated".formatted(name), e);
        }
    }

    private Object doCreateInstance(BeanDefinition... dependencies) throws InvocationTargetException, IllegalAccessException {
        Object[] args = Arrays.stream(beanMethod.getParameters())
                .map(parameter -> getBeanDefinitionByParameter(parameter, dependencies))
                .map(BeanDefinition::getInstance)
                .toArray();

        return beanMethod.invoke(configInstance, args);
    }

    private BeanDefinition getBeanDefinitionByParameter(Parameter parameter, BeanDefinition... dependencies) {
        return Arrays.stream(dependencies)
                .filter(bd -> parameter.getType().isAssignableFrom(bd.type()))
                .filter(bd -> bd.name().equals(resolveParameterName(parameter)))
                .findFirst()
                .orElseThrow(() -> new BeanInstanceCreationException(
                        "'%s' bean has no dependency that matches parameter `%s`".formatted(name, parameter.getName())));
    }

    private String resolveParameterName(Parameter parameter) {
        if (parameter.isAnnotationPresent(Qualifier.class)) {
            return parameter.getAnnotation(Qualifier.class).value();
        }
        return parameter.getName();
    }
}
