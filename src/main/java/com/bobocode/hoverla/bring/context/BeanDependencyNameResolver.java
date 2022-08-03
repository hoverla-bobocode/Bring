package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class BeanDependencyNameResolver {

    public void resolveDependencyNames(BeanDefinitionsContainer container) {
        log.debug("Trying to resolve real dependency names for each bean definition before initialization");
        for (BeanDefinition beanDefinition : container.getBeanDefinitions()) {
            Map<String, Class<?>> beanDependencies = beanDefinition.dependencies();

            if (beanDependencies.isEmpty()) {
                continue;
            }
            log.trace("Verifying names of {} dependencies of bean definition {} - {} ",
                    beanDependencies.size(), beanDefinition.name(), beanDefinition.type().getName());

            List<Pair<String, String>> oldToNewNames = beanDependencies.entrySet()
                    .stream()
                    .map(entry -> resolveDependencyName(entry, beanDefinition, container))
                    .filter(Objects::nonNull)
                    .toList();

            Map<String, Class<?>> oldDependencies = Map.copyOf(beanDependencies);

            oldToNewNames.forEach(namePair -> replaceOldName(namePair, beanDependencies));
            if (oldDependencies.size() != beanDependencies.size()) { // something went wrong if dependency amount changed
                throw new BeanInstanceCreationException("Bean named `%s` has a supertype and one of its subtypes in dependencies and they have the same candidate for injection".formatted(beanDefinition.name()));
            }
        }
    }

    private void replaceOldName(Pair<String, String> oldNameToNewName, Map<String, Class<?>> beanDependencies) {
        String oldName = oldNameToNewName.getLeft();
        String newName = oldNameToNewName.getRight();
        beanDependencies.put(newName, beanDependencies.remove(oldName));
    }

    @Nullable
    private Pair<String, String> resolveDependencyName(Map.Entry<String, Class<?>> targetDependency,
                                                       BeanDefinition rootDefinition,
                                                       BeanDefinitionsContainer container) {
        String dependencyName = targetDependency.getKey();
        Class<?> dependencyType = targetDependency.getValue();

        // if true - dependency has no @Qualifier, thus name resolution is needed
        if (!dependencyName.equals(dependencyType.getName())) {
            return null;
        }

        log.trace("Found dependency with unspecified name: {}. Trying to find matching bean definition by type: {} ",
                dependencyName, dependencyType.getName());

        // check if bean with such name already exists, if yes - no need to resolve name
        if (container.getBeanDefinitionByName(dependencyName).isPresent()) {
            return null;
        }

        List<BeanDefinition> sameTypeBeans = container.getBeansWithExactType(dependencyType);

        // need to make a copy since removal is necessary
        List<BeanDefinition> sameTypeBeansCopy = Lists.newArrayList(sameTypeBeans);
        sameTypeBeansCopy.remove(rootDefinition); // need to remove root to avoid conflicts

        Optional<BeanDefinition> optionalDependency;
        optionalDependency = findMatchingDependency(sameTypeBeansCopy);

        if (optionalDependency.isEmpty()) { // if search by exact type failed - try to do same by assignable type
            List<BeanDefinition> assignableBeans = container.getBeansAssignableFromType(dependencyType);

            List<BeanDefinition> assignableBeansCopy = Lists.newArrayList(assignableBeans);
            assignableBeansCopy.remove(rootDefinition);

            optionalDependency = findMatchingDependency(assignableBeansCopy);
        }

        BeanDefinition matchingDependency = optionalDependency.orElseThrow();
        log.trace("Matching bean definition found: {} - {}", matchingDependency.name(), matchingDependency.type().getName());

        String newDependencyName = matchingDependency.name();
        if (newDependencyName.equals(dependencyName)) {
            log.trace("Matching bean definition has same unspecified name. Aborting replacement");
            return null;
        }

        return Pair.of(dependencyName, newDependencyName);
    }

    private Optional<BeanDefinition> findMatchingDependency(List<BeanDefinition> dependencies) {
        if (dependencies.size() > 1) { // if more than 1 dependency - need to search for primary bean
            log.trace("Found more than 1 bean definitions by type. Will search for single primary bean");
            // we assume that at this point we should have only one primary bean
            // see BeanDefinitionValidator.java
            return dependencies.stream()
                    .filter(BeanDefinition::isPrimary)
                    .findFirst();
        }
        return dependencies.stream().findFirst();
    }
}
