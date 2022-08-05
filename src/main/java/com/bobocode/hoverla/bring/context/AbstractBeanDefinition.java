package com.bobocode.hoverla.bring.context;

import java.util.Map;
import java.util.Objects;

/**
 * Abstract implementation of {@link BeanDefinition} interface with base methods implemented.
 * <p>Resolution of {@code name}, {@code type} and other properties that describe a {@link BeanDefinition}
 * can be found in concrete implementations.</p>
 *
 * @see ConfigBasedBeanDefinition
 * @see ClassBasedBeanDefinition
 */
public abstract class AbstractBeanDefinition implements BeanDefinition {

    protected Object instance;

    protected String name;

    protected Class<?> type;

    protected Map<String, BeanDependency> dependencies;

    /**
     * See {@link BeanDefinition#name()}.
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * See {@link BeanDefinition#type()}.
     */
    @Override
    public Class<?> type() {
        return type;
    }

    /**
     * See {@link BeanDefinition#dependencies()}.
     */
    @Override
    public Map<String, BeanDependency> dependencies() {
        return dependencies;
    }

    /**
     * See {@link BeanDefinition#isInstantiated()}
     *
     * @return {@code true} if {@code instance} of current {@link BeanDefinition} is not null, {@code false} otherwise.
     */
    @Override
    public boolean isInstantiated() {
        return Objects.nonNull(instance);
    }

    /**
     * See {@link BeanDefinition#getInstance()}.
     *
     * @return bean instance of current {@link BeanDefinition}.
     * @throws NullPointerException when {@link ClassBasedBeanDefinition#instance} is null.
     */
    @Override
    public Object getInstance() {
        return Objects.requireNonNull(instance, "Instance of %s has not been created yet".formatted(name));
    }

}
