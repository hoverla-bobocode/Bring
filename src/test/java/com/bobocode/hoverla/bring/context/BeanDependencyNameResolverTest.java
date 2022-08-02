package com.bobocode.hoverla.bring.context;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BeanDependencyNameResolverTest {

    private BeanDependencyNameResolver dependencyNameResolver;

    @BeforeEach
    void setUp() {
        dependencyNameResolver = new BeanDependencyNameResolver();
    }

    @Test
    @DisplayName("Replaces default dependency names with their custom")
    void replacesDefaultNameToCustom() {
        BeanDefinition dependency = prepareDefinition("int1", Integer.class, emptyMap());
        Map<String, Class<?>> dependencies = Maps.newHashMap(Integer.class.getName(), Integer.class);
        BeanDefinition dependent = prepareDefinition("bean", Object.class, dependencies);
        Table<String, Class<?>, BeanDefinition> beanTable = HashBasedTable.create();
        beanTable.put(dependency.name(), dependency.type(), dependency);
        beanTable.put(dependent.name(), dependent.type(), dependent);

        dependencyNameResolver.resolveDependencyNames(beanTable);

        Map<String, Class<?>> resolvedDependencies = dependent.dependencies();

        assertThat(resolvedDependencies)
                .containsEntry(dependency.name(), dependency.type())
                .doesNotContainEntry(Integer.class.getName(), Integer.class);
    }

    @Test
    @DisplayName("Replaces default dependency names with primary bean custom when there are more than 1 bean of the same type")
    void replacesDefaultNameToPrimaryBeanName() {
        BeanDefinition nonPrimaryDependency = prepareDefinition("int1", Integer.class, Map.of());
        BeanDefinition primaryDependency = prepareDefinition("int2", Integer.class, emptyMap());
        when(primaryDependency.isPrimary()).thenReturn(true);
        Map<String, Class<?>> dependencies = Maps.newHashMap(Integer.class.getName(), Integer.class);
        BeanDefinition dependent = prepareDefinition("bean", Object.class, dependencies);

        Table<String, Class<?>, BeanDefinition> beanTable = HashBasedTable.create();
        beanTable.put(nonPrimaryDependency.name(), nonPrimaryDependency.type(), nonPrimaryDependency);
        beanTable.put(primaryDependency.name(), primaryDependency.type(), primaryDependency);
        beanTable.put(dependent.name(), dependent.type(), dependent);

        dependencyNameResolver.resolveDependencyNames(beanTable);

        Map<String, Class<?>> resolvedDependencies = dependent.dependencies();

        assertThat(resolvedDependencies)
                .containsEntry(primaryDependency.name(), primaryDependency.type())
                .doesNotContainEntry(Integer.class.getName(), Integer.class)
                .doesNotContainEntry(nonPrimaryDependency.name(), nonPrimaryDependency.type());
    }

    private BeanDefinition prepareDefinition(String beanName, Class<?> type, Map<String, Class<?>> dependencyMap) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(type).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(beanName);
        when(beanDefinition.dependencies()).thenReturn(dependencyMap);
        return beanDefinition;
    }
}