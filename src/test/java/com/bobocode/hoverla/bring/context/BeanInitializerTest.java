package com.bobocode.hoverla.bring.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeanInitializerTest {

    private static final String BD1 = "beanDef1";
    private static final String BD2 = "beanDef2";
    private static final String BD3 = "beanDef3";
    private static final String BD4 = "beanDef4";

    private BeanInitializer beanInitializer;
    private BeanDependencyNameResolver dependencyNameResolver;

    @BeforeEach
    void setUp() {
        dependencyNameResolver = mock(BeanDependencyNameResolver.class);
        beanInitializer = new BeanInitializer(dependencyNameResolver);
    }

    @Test
    @DisplayName("Initialization test. Verifies that 'instance' method of BeanDefinition is called with correct dependencies")
    void testInitialize() {
        // Given
        BeanDefinition beanDef1 = prepareDefinition(BD1, BD2, BD3);
        BeanDefinition beanDef2 = prepareDefinition(BD2, BD3, BD4);
        BeanDefinition beanDef3 = prepareDefinition(BD3, BD4);
        BeanDefinition beanDef4 = prepareDefinition(BD4);

        List<BeanDefinition> beans = List.of(beanDef1, beanDef2, beanDef3, beanDef4);
        BeanDefinitionsContainer container = new BeanDefinitionsContainer(beans);

        Map<BeanDefinition, BeanDefinition[]> expectedInvocationArgs = Map.of(
                beanDef1, new BeanDefinition[]{beanDef2, beanDef3},
                beanDef2, new BeanDefinition[]{beanDef3, beanDef4},
                beanDef3, new BeanDefinition[]{beanDef4},
                beanDef4, new BeanDefinition[0]
        );

        // When
        beanInitializer.initialize(container);

        // Then
        verify(dependencyNameResolver).resolveDependencyNames(container);

        for (BeanDefinition beanDefinition : container.getBeanDefinitions()) {
            ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

            verify(beanDefinition, times(1)).instantiate(definitionCaptor.capture());

            BeanDefinition[] expectedDependencies = expectedInvocationArgs.get(beanDefinition);
            List<BeanDefinition> actualDependencies = definitionCaptor.getAllValues();

            assertThat(actualDependencies).hasSize(expectedDependencies.length);
            assertThat(actualDependencies).containsExactlyInAnyOrder(expectedDependencies);
        }
    }

    private BeanDefinition prepareDefinition(String beanDefinitionName, String... dependencyNames) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(BeanDefinition.class).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(beanDefinitionName);

        Map<String, BeanDependency> dependencies = new HashMap<>();
        for (var name : dependencyNames) {
            dependencies.put(name, new BeanDependency(name, BeanDefinition.class, null, false));
        }
        when(beanDefinition.dependencies()).thenReturn(dependencies);

        // isInstantiated() method of BeanDefinition should return true only when instantiate(...) method was called.
        // To mock such behavior we basically say: "when instantiate() -> then isInstantiated() should return true"
        Supplier<?> instantiateAnswerSupplier = () -> when(beanDefinition.isInstantiated()).thenReturn(true);
        doAnswer(ignore -> instantiateAnswerSupplier.get()).when(beanDefinition).instantiate(any());

        return beanDefinition;
    }
}