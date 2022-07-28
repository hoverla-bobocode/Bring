package com.bobocode.hoverla.bring.context;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
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

    @Test
    @DisplayName("Initialization test. Verifies that 'instance' method of BeanDefinition is called with correct dependencies")
    void testInitialize() {
        //Given
        BeanDefinition beanDef1 = prepareDefinition(BD1, BD2, BD3);
        BeanDefinition beanDef2 = prepareDefinition(BD2, BD3, BD4);
        BeanDefinition beanDef3 = prepareDefinition(BD3, BD4);
        BeanDefinition beanDef4 = prepareDefinition(BD4);

        Table<String, Class<?>, BeanDefinition> beanDefinitionTable = ImmutableTable.<String, Class<?>, BeanDefinition>builder()
                .put(Tables.immutableCell(beanDef1.name(), beanDef1.type(), beanDef1))
                .put(Tables.immutableCell(beanDef2.name(), beanDef2.type(), beanDef2))
                .put(Tables.immutableCell(beanDef3.name(), beanDef3.type(), beanDef3))
                .put(Tables.immutableCell(beanDef4.name(), beanDef4.type(), beanDef4))
                .build();

        Map<BeanDefinition, BeanDefinition[]> expectedInvocationArgs = Map.of(
                beanDef1, new BeanDefinition[]{beanDef2, beanDef3},
                beanDef2, new BeanDefinition[]{beanDef3, beanDef4},
                beanDef3, new BeanDefinition[]{beanDef4},
                beanDef4, new BeanDefinition[0]
        );

        BeanInitializer beanInitializer = new BeanInitializer();

        //When
        beanInitializer.initialize(beanDefinitionTable);

        //Then
        for (BeanDefinition beanDefinition : beanDefinitionTable.values()) {
            ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

            verify(beanDefinition, times(1)).instantiate(definitionCaptor.capture());

            BeanDefinition[] expectedDependencies = expectedInvocationArgs.get(beanDefinition);
            List<BeanDefinition> actualDependencies = definitionCaptor.getAllValues();

            assertThat(actualDependencies).hasSize(expectedDependencies.length);
            assertThat(actualDependencies).containsExactlyInAnyOrder(expectedDependencies);
        }
    }

    private static BeanDefinition prepareDefinition(String beanDefinitionName, String... dependencyNames) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(BeanDefinition.class).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(beanDefinitionName);

        Map<String, Class<?>> dependencies = new HashMap<>();
        for (var name : dependencyNames) {
            dependencies.put(name, BeanDefinition.class);
        }
        when(beanDefinition.dependencies()).thenReturn(dependencies);

        // isInstantiated() method of BeanDefinition should return true only when instantiate(...) method was called.
        // To mock such behavior we basically say: "when instantiate -> then isInstantiated() should return true"
        Supplier<?> instantiateAnswerSupplier = () -> when(beanDefinition.isInstantiated()).thenReturn(true);
        doAnswer(ignore -> instantiateAnswerSupplier.get()).when(beanDefinition).instantiate(any());

        return beanDefinition;
    }
}