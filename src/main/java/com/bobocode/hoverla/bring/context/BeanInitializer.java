package com.bobocode.hoverla.bring.context;

import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * Class, responsible for triggering bean initialization.
 * <br>
 * Its main aim is to pass all dependencies of a particular {@link BeanDefinition}
 * to its {@link BeanDefinition#instance(BeanDefinition...)} method.
 */
@Slf4j
public class BeanInitializer {


    /**
     * Triggers instantiation of all beans by passing their required dependencies.
     * @param beanDefinitionsTable {@link Table} with all {@link BeanDefinition} objects handled by current context.
     */
    public void initialize(Table<String, Class<?>, BeanDefinition> beanDefinitionsTable) {
        log.debug("Bean initialization started");
        Collection<BeanDefinition> beanDefinitions = beanDefinitionsTable.values();

        for (BeanDefinition beanDefinition : beanDefinitions) {
            log.trace("Resolving dependencies for bean definition: {} - {}", beanDefinition.name(), beanDefinition.type());
            BeanDefinition[] beanDependencies = beanDefinition.dependencies()
                    .entrySet()
                    .stream()
                    .map(keyPair -> beanDefinitionsTable.get(keyPair.getKey(), keyPair.getValue()))
                    .toArray(BeanDefinition[]::new);

            log.trace("Found {} dependencies", beanDependencies.length);
            beanDefinition.instance(beanDependencies);
        }
    }
}
