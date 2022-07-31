package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanValidationException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.StringUtils.CR;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.containsAny;

/**
 * Validates beans list to avoid such cases:
 * <ol>
 *   <li>duplicate names</li>
 *   <li>bean name is empty, contains spaces, carriage return, new line or tab symbols</li>
 *   <li>found by name bean of dependency is not suitable by type</li>
 *   <li>circular dependency</li>
 *   <li>application context doesn't contain the required bean dependency</li>
 * </ol>
 * <p>
 * How it validates:
 *     <li>The first step is checking the list for null</li>
 *     <li>The next one is validation for duplicate names in the list</li>
 *     <li>After that, validate bean definition name {@link #validateName}</li>
 *     <li>Then start checking dependencies for each bean definition {@link #validateDependencies}:
 *     <ol>
 *         <li>Create the {@code Map<String, List<String>> requiredDependencyNames} for this bean definition.
 *         Key - bean name; value - list of dependencies for this bean. This map contains all dependencies which are
 *         needed for creating the bean of the current bean definition</li>
 *         <li>After that, check each dependency {@link #validateDependency}</li>
 *         <li>Next validate dependency name {@link #validateName}</li>
 *         <li>Finding required BeanDefinition for dependency via: {@link #resolveDependency}. This method first finds
 *         by name, if exists set it, else find it by type. Throws an exception if was not found. Finding by name is also
 *         validated by type matching {@link #checkTypeMatching}</li>
 *         <li>When Bean definition was found, start validating on circular dependency {@link #checkCircularDependency}
 *         root bean definition and found bean definition.</li>
 *         <li>During circular validation, check if the dependencies of the found bean contain the root bean. After that,
 *         check that dependencies of found bean are in requiredDependencyNames, if no - put it in.
 *         Find BeanDefinition for these dependencies in {@link #checkInnerDependencies} and make recursive
 *         call {@link #checkCircularDependency} each found beans</li>
 *     </ol>
 *     </li>
 */
@Slf4j
public class BeanDefinitionValidator {

    private static final String BEAN_DEFINITION_LIST_IS_NULL = "Bean definition list is null";
    private static final String DUPLICATE_BEAN_NAMES = "Context contains beans with duplicate names: %s";
    private static final String INCORRECT_BEAN_NAME = "%s name for %s class is incorrect. It must not be empty, or contain carriage return, new line, space or tab symbols";
    private static final String DIFFERENT_TYPES_IN_DEPENDENCIES = "Found dependency type is not assignable from the required type - required %s but found - %s";
    private static final String MULTIPLE_BEANS_WITH_TYPE = "Found more than 1 bean with type: %s in context";
    private static final String NOT_FOUND_BEANS = "Unable to find bean with name - `%s` and type - %s in context";
    private static final String BEAN = "Bean";
    private static final String DEPENDENCY = "Dependency";

    private static final CharSequence[] ILLEGAL_CHARACTERS = {SPACE, LF, CR, "\t"};

    private final Table<String, Class<?>, BeanDefinition> beanDefinitionCache = HashBasedTable.create();

    /**
     * @param beanDefinitions all bean definitions of application context
     * @throws NullPointerException    if beanDefinitions is null
     * @throws BeanValidationException if there are conflicts: bean name is empty, contains spaces, carriage return,
     *                                 new line or tab symbols, found by name bean of dependency is not suitable by type,
     *                                 circular dependency, application context doesn't contain the required bean
     */
    public void validate(List<BeanDefinition> beanDefinitions) {
        Objects.requireNonNull(beanDefinitions, BEAN_DEFINITION_LIST_IS_NULL);
        log.info("Bean validation started. Received {} bean definitions", beanDefinitions.size());

        validateDuplicateNames(beanDefinitions);

        for (BeanDefinition beanDefinition : beanDefinitions) {
            validateName(beanDefinition.name(), beanDefinition.type(), BEAN);
            validateDependencies(beanDefinition, beanDefinitions);
        }
    }

    private void validateDuplicateNames(List<BeanDefinition> beanDefinitions) {
        Map<String, Long> nameCountMap = beanDefinitions.stream()
                .collect(groupingBy(BeanDefinition::name, counting()));

        Set<String> duplicateNames = Maps.filterValues(nameCountMap, nameCount -> nameCount > 1).keySet();

        if (!duplicateNames.isEmpty()) {
            throw new BeanValidationException(DUPLICATE_BEAN_NAMES.formatted(duplicateNames));
        }
    }

    private void validateName(String name, Class<?> type, String checkInstance) {
        String typeName = type.getName();
        log.trace("Validating name for {}: {} - {}", checkInstance, name, typeName);

        if (name.isBlank() || containsAny(name, ILLEGAL_CHARACTERS)) {
            throw new BeanValidationException(INCORRECT_BEAN_NAME.formatted(checkInstance, typeName));
        }
    }

    private void validateDependencies(BeanDefinition currentBeanDefinition, List<BeanDefinition> allDefinitions) {
        String beanDefName = currentBeanDefinition.name();
        log.trace("Checking currentDependencies for bean definition: {} - {}", beanDefName, currentBeanDefinition.type());

        Map<String, Class<?>> currentDependencies = currentBeanDefinition.dependencies();
        if (currentDependencies.isEmpty()) {
            return;
        }
        log.trace("{} dependencies found", currentDependencies.size());
        Set<String> dependencyNames = currentDependencies.keySet();

        Map<String, Set<String>> requiredDependencyNames = new LinkedHashMap<>();
        requiredDependencyNames.put(beanDefName, dependencyNames);

        for (Map.Entry<String, Class<?>> dependencyEntry : currentDependencies.entrySet()) {
            String dependencyName = dependencyEntry.getKey();
            Class<?> dependencyType = dependencyEntry.getValue();

            validateName(dependencyName, dependencyType, DEPENDENCY);
            validateDependency(currentBeanDefinition, allDefinitions, requiredDependencyNames, dependencyEntry);
        }
    }

    private void validateDependency(BeanDefinition currentBeanDefinition,
                                    List<BeanDefinition> allDefinitions,
                                    Map<String, Set<String>> requiredDependencyNames,
                                    Map.Entry<String, Class<?>> dependencyEntry) {
        String dependencyName = dependencyEntry.getKey();
        Class<?> dependencyType = dependencyEntry.getValue();
        log.trace("Checking dependency: {} - {}", dependencyName, dependencyType.getName());

        BeanDefinition foundDependency = resolveDependency(dependencyName, dependencyType, allDefinitions);
        checkCircularDependency(currentBeanDefinition, foundDependency, allDefinitions, requiredDependencyNames);
    }

    private BeanDefinition resolveDependency(String dependencyName, Class<?> dependencyType,
                                             List<BeanDefinition> allDefinitions) {
        BeanDefinition cachedDefinition = beanDefinitionCache.get(dependencyName, dependencyType);
        if (Objects.nonNull(cachedDefinition)) {
            return cachedDefinition;
        }

        BeanDefinition foundDependency;
        Optional<BeanDefinition> definitionByName = findByName(dependencyName, allDefinitions);
        if (definitionByName.isPresent()) {
            foundDependency = definitionByName.get();
            checkTypeMatching(dependencyType, foundDependency);
        } else {
            // Check whether @qualifier contains a non-existing bean name
            if (!dependencyName.equals(dependencyType.getName())) {
                throw new BeanValidationException(NOT_FOUND_BEANS.formatted(dependencyName, dependencyType.getName()));
            }

            log.warn("Was not able to find bean by name `{}` - trying to find by type: {}",
                    dependencyName, dependencyType.getName());
            foundDependency = tryFindByType(dependencyType, allDefinitions, dependencyName);
            log.warn("Found bean `{}` of class {}", foundDependency.name(), foundDependency.type().getName());
        }

        beanDefinitionCache.put(dependencyName, dependencyType, foundDependency);
        return foundDependency;
    }

    private void checkCircularDependency(BeanDefinition rootBean,
                                         BeanDefinition foundDependency,
                                         List<BeanDefinition> allDefinitions,
                                         Map<String, Set<String>> requiredDependencyNames) {
        Map<String, Class<?>> innerDependencies = foundDependency.dependencies();

        if (innerDependencies.isEmpty()) {
            return;
        }
        String rootBeanName = rootBean.name();
        String foundDependencyName = foundDependency.name();

        innerDependencies.computeIfPresent(rootBeanName, (ig, ig2) -> {
            throw validationException(foundDependency, requiredDependencyNames);
        });

        for (Map.Entry<String, Class<?>> innerDependencyEntry : innerDependencies.entrySet()) {
            String dependencyName = innerDependencyEntry.getKey();
            Class<?> dependencyType = innerDependencyEntry.getValue();

            validateName(dependencyName, dependencyType, DEPENDENCY);

            requiredDependencyNames.computeIfPresent(dependencyName, (ig, ig2) -> {
                throw validationException(foundDependency, requiredDependencyNames);
            });

            requiredDependencyNames.put(foundDependencyName, innerDependencies.keySet());
            checkInnerDependencies(rootBean, innerDependencyEntry, allDefinitions, requiredDependencyNames);
        }
    }

    private void checkInnerDependencies(BeanDefinition root, Map.Entry<String, Class<?>> innerDependencyEntry,
                                        List<BeanDefinition> allDefinitions,
                                        Map<String, Set<String>> requiredDependencyNames) {
        String innerDependencyName = innerDependencyEntry.getKey();
        Class<?> innerDependencyType = innerDependencyEntry.getValue();
        BeanDefinition foundInnerDependency = resolveDependency(innerDependencyName, innerDependencyType, allDefinitions);

        checkCircularDependency(root, foundInnerDependency, allDefinitions, requiredDependencyNames);
    }

    private BeanValidationException validationException(BeanDefinition dependency,
                                                        Map<String, Set<String>> requiredDependencyNames) {
        String message = buildCircularExceptionMessage(dependency, requiredDependencyNames);
        return new BeanValidationException(message);
    }

    private String buildCircularExceptionMessage(BeanDefinition found,
                                                 Map<String, Set<String>> requiredDependencies) {
        StringBuilder message = new StringBuilder("Oops. Circular dependency occurs with bean: " + found.name() + " - "
                + found.type().getName() + "\n");
        String template = "%s depends on: %s";
        for (Map.Entry<String, Set<String>> dep : requiredDependencies.entrySet()) {
            message.append(template.formatted(dep.getKey(), dep.getValue())).append("\n");
        }
        message.append(template.formatted(found.name(), found.dependencies().keySet()));
        return message.toString();
    }

    private Optional<BeanDefinition> findByName(String targetName, List<BeanDefinition> allDefinitions) {
        return allDefinitions.stream()
                .filter(bd -> targetName.equals(bd.name()))
                .findAny();
    }

    private void checkTypeMatching(Class<?> requiredType, BeanDefinition foundBeanDefinition) {
        Class<?> foundType = foundBeanDefinition.type();
        if (!requiredType.isAssignableFrom(foundType)) {
            throw new BeanValidationException(DIFFERENT_TYPES_IN_DEPENDENCIES.formatted(requiredType.getName(), foundType));
        }
    }

    private BeanDefinition tryFindByType(Class<?> type, List<BeanDefinition> beanDefinitions, String name) {
        List<BeanDefinition> beansByType = findByType(type, beanDefinitions);
        if (beansByType.size() > 1) {
            throw new BeanValidationException(MULTIPLE_BEANS_WITH_TYPE.formatted(type));
        }
        if (beansByType.isEmpty()) {
            throw new BeanValidationException(NOT_FOUND_BEANS.formatted(name, type.getName()));
        }
        return beansByType.get(0);
    }

    private List<BeanDefinition> findByType(Class<?> targetType, List<BeanDefinition> allDefinitions) {
        return allDefinitions.stream()
                .filter(bd -> targetType.isAssignableFrom(bd.type()))
                .toList();
    }
}