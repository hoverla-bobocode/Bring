package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionConfusingDependencies;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionConstructorFieldInjection;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionConstructorInjection;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionFieldInjection;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionMultipleConstructors;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionNoDependencies;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionNoName;
import com.bobocode.hoverla.bring.test.subject.definition.ClassBasedBeanDefinitionWithName;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean1;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean2;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean3;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean4;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean5;
import com.bobocode.hoverla.bring.test.subject.definition.TestBeanWithSingleArgumentInjectConstructor;
import com.bobocode.hoverla.bring.test.subject.definition.TestBeanWithoutDependencies;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.bobocode.hoverla.bring.support.BeanDefinitionAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassBasedBeanDefinitionTest {

    private Stream<Arguments> resolveNameArgs() {
        return Stream.of(
                Arguments.of(TestBean1.class, "testBean1", "Name is resolved from @Bean annotation"),
                Arguments.of(TestBean2.class, TestBean2.class.getName(), "Name is resolved from bean type")
        );
    }

    private Stream<Arguments> resolveDependenciesArgs() {
        return Stream.of(
                Arguments.of(TestBean1.class,
                        Map.of(String.class.getName(), String.class,
                                Integer.class.getName(), Integer.class,
                                Double.class.getName(), Double.class),
                        "Plain constructor dependencies are resolved"
                ),
                Arguments.of(TestBean2.class,
                        Map.of(TestBean1.class.getName(), TestBean1.class),
                        "@Inject constructor dependencies are resolved"
                ),
                Arguments.of(TestBean3.class,
                        Map.of(TestBean1.class.getName(), TestBean1.class,
                                TestBean2.class.getName(), TestBean2.class),
                        "@Inject constructor and field dependencies are resolved"
                ),
                Arguments.of(TestBean4.class,
                        Map.of(TestBean2.class.getName(), TestBean2.class,
                                TestBean3.class.getName(), TestBean3.class),
                        "@Inject field dependencies are resolved"
                ),
                Arguments.of(TestBean5.class,
                        Map.of("bean1", TestBean1.class,
                                "bean4", TestBean4.class),
                        "Constructor and field dependencies are resolved with names from @Qualifier")
        );
    }

    private Stream<Arguments> instantiationArgs() {
        return Stream.of(
                Arguments.of(TestBeanWithoutDependencies.class, new BeanDefinition[0],
                        "Instantiate bean without dependencies"
                ),
                Arguments.of(TestBeanWithSingleArgumentInjectConstructor.class,
                        prepareDefinitions(TestBeanWithoutDependencies.class),
                        "Instantiate bean with 1-argument @Inject constructor"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] - Resolve name - {2}")
    @MethodSource("resolveNameArgs")
    void resolveNameTest(Class<?> beanClass, String expectedName, String description) {
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);
        assertThat(beanDefinition)
                .hasName(expectedName)
                .hasType(beanClass); // just to cover getter for type
    }

    @ParameterizedTest(name = "[{index}] - Resolve dependencies - {2}")
    @MethodSource("resolveDependenciesArgs")
    void resolveDependenciesTest(Class<?> beanClass, Map<String, Class<?>> expectedDependencies, String description) {
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);
        assertThat(beanDefinition).hasDependencies(expectedDependencies);
    }


    /*
    1. Bean without dependencies - done
    2. Bean with constructor but without fields - done
    3. Bean without constructor dependencies but with fields
    4. Bean with constructor dependencies and fields
    5. ...
    */
    @ParameterizedTest(name = "[{index}] - Instantiation - {2}")
    @MethodSource("instantiationArgs")
    void instantiationTest(Class<?> beanClass, BeanDefinition[] dependencies, String description) {
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);
        Object instance = beanDefinition.instance(dependencies);

        Assertions.assertThat(instance)
                .isNotNull()
                .isInstanceOf(beanClass);
    }

    private BeanDefinition[] prepareDefinitions(Class<?>... beanClasses) {
        return Arrays.stream(beanClasses)
                .map(ClassBasedBeanDefinition::new)
                .toArray(BeanDefinition[]::new);
    }

    // THESE TESTS ARE TO BE REMOVED
    @Test
    @Disabled
    @DisplayName("Create instance of a bean with constructor injection")
    void returnBeanWithConstructorInjection() {
        // given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionConstructorInjection.class);

        final ClassBasedBeanDefinitionNoName dependencyInstance1 = new ClassBasedBeanDefinitionNoName();
        final ClassBasedBeanDefinitionWithName dependencyInstance2 = new ClassBasedBeanDefinitionWithName();
        final ClassBasedBeanDefinitionConstructorInjection expectedBean =
                new ClassBasedBeanDefinitionConstructorInjection(dependencyInstance1, dependencyInstance2);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.name()).thenReturn(ClassBasedBeanDefinitionNoName.class.getName());
        when(dependency2.name()).thenReturn(ClassBasedBeanDefinitionWithName.class.getName());

        when(dependency1.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoName.class);
        when(dependency2.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionWithName.class);

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);

        // when
        final Object instance = beanDefinition.instance(dependency1, dependency2);

        // then
        assertNotNull(instance);
        assertEquals(expectedBean.getClass(), instance.getClass());

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
    }
    @Test
    @Disabled
    @DisplayName("Create instance of a bean with field injection")
    void returnBeanWithFieldInjection() {
        // given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionFieldInjection.class);

        final ClassBasedBeanDefinitionFieldInjection expectedBean =
                new ClassBasedBeanDefinitionFieldInjection();

        final ClassBasedBeanDefinitionNoName dependencyInstance1 = new ClassBasedBeanDefinitionNoName();
        expectedBean.setClassBasedBeanDefinitionNoName(dependencyInstance1);
        final ClassBasedBeanDefinitionWithName dependencyInstance2 = new ClassBasedBeanDefinitionWithName();
        expectedBean.setClassBasedBeanDefinitionWithName(dependencyInstance2);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.name()).thenReturn(ClassBasedBeanDefinitionNoName.class.getName());
        when(dependency2.name()).thenReturn(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME);

        when(dependency1.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoName.class);
        when(dependency2.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionWithName.class);

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);

        // when
        final Object instance = beanDefinition.instance(dependency1, dependency2);

        // then
        assertNotNull(instance);
        assertEquals(expectedBean.getClass(), instance.getClass());

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
    }

    @Test
    @Disabled
    @DisplayName("Create instance of a bean with constructor and field injection")
    void returnBeanWithConstructorAndFieldInjection() {
        // given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionConstructorFieldInjection.class);

        final ClassBasedBeanDefinitionNoName dependencyInstance1 = new ClassBasedBeanDefinitionNoName();
        final ClassBasedBeanDefinitionWithName dependencyInstance2 = new ClassBasedBeanDefinitionWithName();
        final ClassBasedBeanDefinitionNoDependencies dependencyInstance3 = new ClassBasedBeanDefinitionNoDependencies();
        final ClassBasedBeanDefinitionConstructorFieldInjection expectedBean =
                new ClassBasedBeanDefinitionConstructorFieldInjection(dependencyInstance2, dependencyInstance3);
        expectedBean.setClassBasedBeanDefinitionNoName(dependencyInstance1);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency3 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.name()).thenReturn(ClassBasedBeanDefinitionNoName.class.getName());
        when(dependency2.name()).thenReturn(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME);
        when(dependency3.name()).thenReturn(ClassBasedBeanDefinitionNoDependencies.class.getName());

        when(dependency1.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoName.class);
        when(dependency2.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionWithName.class);
        when(dependency3.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoDependencies.class);

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);
        when(dependency3.instance()).thenReturn(dependencyInstance3);

        // when
        final Object instance = beanDefinition.instance(dependency1, dependency2, dependency3);

        // then
        assertNotNull(instance);
        assertEquals(expectedBean.getClass(), instance.getClass());

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
        verify(dependency3).instance(any());
    }

    @Test
    @Disabled
    @DisplayName("Create bean instance. Dependencies return null. Throws BeanInstanceCreationException")
    void throwBeanInstanceCreationExceptionWhenDependenciesAreNull() {
        // given
        final String expectedMessage = "'%s' bean can't be instantiated".formatted(ClassBasedBeanDefinitionFieldInjection.class.getSimpleName());
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionFieldInjection.class);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.name()).thenReturn(ClassBasedBeanDefinitionNoName.class.getName());
        when(dependency2.name()).thenReturn(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME);

        when(dependency1.instance()).thenReturn(null);
        when(dependency2.instance()).thenReturn(null);

        // then
        Assertions.assertThatThrownBy(
                        () -> beanDefinition.instance(dependency1, dependency2))
                .isInstanceOf(BeanInstanceCreationException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    @Disabled
    @DisplayName("Create bean instance. Parameters are nulls. Throws BeanInstanceCreationException")
    void throwBeanInstanceCreationExceptionWhenParameterAreNull() {
        // given
        final String expectedMessage = "'%s' bean can't be instantiated".formatted(ClassBasedBeanDefinitionFieldInjection.class.getSimpleName());
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionFieldInjection.class);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.name()).thenReturn(ClassBasedBeanDefinitionNoName.class.getSimpleName());
        when(dependency2.name()).thenReturn(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME);

        when(dependency1.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoName.class);
        when(dependency2.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionWithName.class);

        // then
        Assertions.assertThatThrownBy(
                        () -> beanDefinition.instance(null, null))
                .isInstanceOf(BeanInstanceCreationException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    @Disabled
    @DisplayName("Create instance of a bean with confusing dependencies")
    void returnBeanWithConfusingDependencies() {
        // given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionConfusingDependencies.class);

        final ClassBasedBeanDefinitionNoName dependencyInstance1 = new ClassBasedBeanDefinitionNoName();
        final ClassBasedBeanDefinitionWithName dependencyInstance2 = new ClassBasedBeanDefinitionWithName();
        final ClassBasedBeanDefinitionNoDependencies dependencyInstance3 = new ClassBasedBeanDefinitionNoDependencies();
        final ClassBasedBeanDefinitionConfusingDependencies expectedBean =
                new ClassBasedBeanDefinitionConfusingDependencies(dependencyInstance2, dependencyInstance3);
        expectedBean.setClassBasedBeanDefinitionNoName(dependencyInstance1);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency3 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.name()).thenReturn(ClassBasedBeanDefinitionNoName.class.getName());
        when(dependency2.name()).thenReturn(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME);
        when(dependency3.name()).thenReturn(ClassBasedBeanDefinitionNoDependencies.class.getName());

        when(dependency1.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoName.class);
        when(dependency2.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionWithName.class);
        when(dependency3.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoDependencies.class);

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);
        when(dependency3.instance()).thenReturn(dependencyInstance3);

        // when
        final Object instance = beanDefinition.instance(dependency1, dependency2, dependency3);

        // then
        assertNotNull(instance);
        assertEquals(expectedBean.getClass(), instance.getClass());

        verify(dependency1).instance(any());
        verify(dependency2, times(2)).instance(any());
        verify(dependency3).instance(any());
    }

    @Test
    @Disabled
    @DisplayName("Create instance of a bean with multiple constructors")
    void testInstance_ReturnBeanWithMultipleConstructors() {
        // given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionMultipleConstructors.class);

        final ClassBasedBeanDefinitionNoName dependencyInstance1 = new ClassBasedBeanDefinitionNoName();
        final ClassBasedBeanDefinitionWithName dependencyInstance2 = new ClassBasedBeanDefinitionWithName();
        final ClassBasedBeanDefinitionNoDependencies dependencyInstance3 = new ClassBasedBeanDefinitionNoDependencies();
        final ClassBasedBeanDefinitionMultipleConstructors expectedBean =
                new ClassBasedBeanDefinitionMultipleConstructors(dependencyInstance1, dependencyInstance2, dependencyInstance3);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency3 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.name()).thenReturn(ClassBasedBeanDefinitionNoName.class.getName());
        when(dependency2.name()).thenReturn(ClassBasedBeanDefinitionWithName.class.getName());
        when(dependency3.name()).thenReturn(ClassBasedBeanDefinitionNoDependencies.class.getName());

        when(dependency1.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoName.class);
        when(dependency2.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionWithName.class);
        when(dependency3.type()).thenAnswer(invocationOnMock -> ClassBasedBeanDefinitionNoDependencies.class);

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);
        when(dependency3.instance()).thenReturn(dependencyInstance3);

        // when
        final Object instance = beanDefinition.instance(dependency1, dependency2, dependency3);

        // then
        assertNotNull(instance);
        assertEquals(expectedBean.getClass(), instance.getClass());

        verify(dependency1).instance(any());
        verify(dependency2, times(2)).instance(any());
        verify(dependency3).instance(any());
    }
}
