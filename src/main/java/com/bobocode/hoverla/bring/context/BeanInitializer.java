package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.exception.BeanInitializePhaseException;
import com.bobocode.hoverla.bring.exception.NoSuchBeanException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;

/**
 * Class, responsible for triggering bean initialization.
 *
 * <p>Its main aim is to find all dependencies of a particular {@link BeanDefinition} and pass them
 * to its {@link BeanDefinition#instantiate(BeanDefinition...)} method.</p>
 *
 * @see Bean @Bean
 * @see BeanDefinition
 */
@Slf4j
@RequiredArgsConstructor
public class BeanInitializer {

    private final BeanDependencyNameResolver dependencyNameResolver;

    /**
     * Triggers instantiation of all beans by passing their required dependencies.
     *
     * @param container {@link BeanDefinitionsContainer} with all {@link BeanDefinition} objects handled by current context.
     * @throws BeanInitializePhaseException in case an unexpected error occurs.
     */
    public void initialize(BeanDefinitionsContainer container) {
        log.debug("Bean initialization started");
        dependencyNameResolver.resolveDependencyNames(container);

        Collection<BeanDefinition> beanDefinitions = container.getBeanDefinitions();
        try {
            beanDefinitions.forEach(beanDefinition -> doInitialize(beanDefinition, container));
        } catch (Exception ex) {
            throw new BeanInitializePhaseException("An error occurred during initialization phase", ex);
        }
    }

    private void doInitialize(BeanDefinition definitionToInitialize, BeanDefinitionsContainer container) {
        if (definitionToInitialize.isInstantiated()) {
            return;
        }
        String beanName = definitionToInitialize.name();
        log.trace("Initializing bean definition of name `{}`", beanName);

        BeanDefinition[] beanDependencies = getBeanDependencies(definitionToInitialize, container);
        int numberOfDependencies = ArrayUtils.getLength(beanDependencies);

        if (numberOfDependencies == 0) {
            definitionToInitialize.instantiate();
            return;
        }

        log.trace("Found {} dependencies for bean with name `{}`", numberOfDependencies, beanName);
        for (BeanDefinition beanDependency : beanDependencies) {
            if (!beanDependency.isInstantiated()) {
                doInitialize(beanDependency, container);
            }
        }
        definitionToInitialize.instantiate(beanDependencies);
    }

    private BeanDefinition[] getBeanDependencies(BeanDefinition root, BeanDefinitionsContainer container) {
        return root.dependencies()
                .keySet()
                .stream()
                .map(dependencyName -> tryGetDependencyByName(dependencyName, root, container))
                .toArray(BeanDefinition[]::new);
    }

    private BeanDefinition tryGetDependencyByName(String dependencyName, BeanDefinition root,
                                                  BeanDefinitionsContainer container) {
        return container.getBeanDefinitionByName(dependencyName)
                .orElseThrow(() -> new NoSuchBeanException("Unable to resolve dependency '%s' of bean '%s"
                        .formatted(dependencyName, root.name())));
    }
}
