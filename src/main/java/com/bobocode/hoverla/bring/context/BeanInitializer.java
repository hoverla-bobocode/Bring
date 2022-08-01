package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.google.common.collect.Table;
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
public class BeanInitializer {

    /**
     * Triggers instantiation of all beans by passing their required dependencies.
     *
     * @param beanDefinitionsTable {@link Table} with all {@link BeanDefinition} objects handled by current context.
     */
    public void initializeBeans(Table<String, Class<?>, BeanDefinition> beanDefinitionsTable) {
        log.debug("Bean initialization started");
        Collection<BeanDefinition> beanDefinitions = beanDefinitionsTable.values();
        beanDefinitions.forEach(beanDefinition -> initializeBean(beanDefinition, beanDefinitionsTable));
    }

    public void initializeBean(BeanDefinition definitionToInitialize,
                               Table<String, Class<?>, BeanDefinition> beanDefinitionsTable) {
        if (definitionToInitialize.isInstantiated()) {
            return;
        }
        String beanName = definitionToInitialize.name();
        log.trace("Resolving dependencies for bean definition: {}", beanName);

        BeanDefinition[] beanDependencies = getBeanDependencies(definitionToInitialize, beanDefinitionsTable);
        int numberOfDependencies = ArrayUtils.getLength(beanDependencies);

        if (numberOfDependencies == 0) {
            definitionToInitialize.instantiate();
            return;
        }

        log.trace("Found {} dependencies for {}", numberOfDependencies, beanName);
        for (BeanDefinition beanDependency : beanDependencies) {
            if (!beanDependency.isInstantiated()) {
                initializeBean(beanDependency, beanDefinitionsTable);
            }
        }
        definitionToInitialize.instantiate(beanDependencies);
    }

    private BeanDefinition[] getBeanDependencies(BeanDefinition root,
                                                 Table<String, Class<?>, BeanDefinition> beanDefinitionsTable) {
        return root.dependencies()
                .entrySet()
                .stream()
                .map(keyPair -> beanDefinitionsTable.get(keyPair.getKey(), keyPair.getValue()))
                .toArray(BeanDefinition[]::new);
    }
}
