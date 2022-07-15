package com.bobocode.hoverla.bring.context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

class BeanInitializerTest {

    private static final String BD1 = "beanDef1";
    private static final String BD2 = "beanDef2";
    private static final String BD3 = "beanDef3";
    private static final String BD4 = "beanDef4";

    private static Stream<Arguments> testArgs() {
        BeanDefinition beanDef1 = prepare(BD1, List.of(BD2, BD3));
        BeanDefinition beanDef2 = prepare(BD2, List.of(BD3, BD4));
        BeanDefinition beanDef3 = prepare(BD3, singletonList(BD4));
        BeanDefinition beanDef4 = prepare(BD4, emptyList());

        return Stream.of(
                Arguments.of(
                        Map.of(
                                BD1, beanDef1, BD2, beanDef2,
                                BD3, beanDef3, BD4, beanDef4
                        ),
                        Map.of(
                                beanDef1, new BeanDefinition[]{beanDef2, beanDef3},
                                beanDef2, new BeanDefinition[]{beanDef3, beanDef4},
                                beanDef3, new BeanDefinition[]{beanDef4},
                                beanDef4, new BeanDefinition[0]
                        ),
                        "Bean initialization default test case"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] - {2}")
    @MethodSource("testArgs")
    void testInitialize(Map<String, BeanDefinition> beanDefinitionMap,
                        Map<BeanDefinition, BeanDefinition[]> expectedInvocationArgs, String description) {
        var beanInitializer = new BeanInitializer();
        beanInitializer.initialize(beanDefinitionMap);

        beanDefinitionMap.values().forEach(bd -> verify(bd).instance(expectedInvocationArgs.get(bd)));
    }


    private static BeanDefinition prepare(String name, List<String> dependenciesNames) {
        BeanDefinition beanDefinition = spy(BeanDefinition.class);
        when(beanDefinition.name()).thenReturn(name);
        when(beanDefinition.dependenciesNames()).thenReturn(dependenciesNames);

        return beanDefinition;
    }
}