package com.bobocode.hoverla.bring.context;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Map;

@Slf4j
public class BeanDependencyNameResolver {

    public void resolveDependencyNames(Table<String, Class<?>, BeanDefinition> beanDefinitions) {
        log.debug("Trying to resolve real dependency names for each bean definition before initialization");
        for (BeanDefinition beanDefinition : beanDefinitions.values()) {
            Map<String, Class<?>> beanDependencies = beanDefinition.dependencies();

            log.trace("Verifying names of {} dependencies of bean definition {} - {} ",
                    beanDependencies.size(), beanDefinition.name(), beanDefinition.type().getName());

            beanDependencies.entrySet()
                    .iterator() // using Iterator to avoid ConcurrentModificationException
                    .forEachRemaining(entry -> resolveDependencyName(entry, beanDependencies, beanDefinitions));
        }
    }

    private void resolveDependencyName(Map.Entry<String, Class<?>> targetDependency,
                                       Map<String, Class<?>> beanDependencies,
                                       Table<String, Class<?>, BeanDefinition> beanDefinitions) {
        String dependencyName = targetDependency.getKey();
        Class<?> dependencyType = targetDependency.getValue();

        if (dependencyName.equals(dependencyType.getName())) { // mean dependency has no @Qualifier annotation
            log.trace("Found dependency with unspecified name: {}. Trying to find matching bean definition by type: {} ",
                    dependencyName, dependencyType.getName());

            Map<String, BeanDefinition> definitionsByType = beanDefinitions.column(dependencyType);
            BeanDefinition matchingDependency = findMatchingDependency(definitionsByType);
            log.trace("Matching bean definition found: {} - {}", matchingDependency.name(), matchingDependency.type().getName());

            String newDependencyName = matchingDependency.name();
            if (newDependencyName.equals(dependencyName)) {
                log.trace("Matching bean definition has same unspecified name. Aborting replacement");
                return;
            }

            Class<?> oldDependencyType = beanDependencies.remove(dependencyName);
            beanDependencies.put(newDependencyName, oldDependencyType);
            log.trace("Replaced unspecified name {} with {}", dependencyName, newDependencyName);
        }
    }

    private BeanDefinition findMatchingDependency(Map<String, BeanDefinition> definitionsByType) {
        if (definitionsByType.size() > 1) { // need to search for primary bean
            log.trace("Found more than 1 bean definitions by type. Will search for single primary bean");
            // we assume that at this point we should have only one primary bean
            // see BeanDefinitionValidator.java
            Map<String, BeanDefinition> primaryBeanMap = Maps.filterValues(definitionsByType, BeanDefinition::isPrimary);
            return CollectionUtils.get(primaryBeanMap, 0).getValue();
        }
        return CollectionUtils.get(definitionsByType, 0).getValue();
    }
}
