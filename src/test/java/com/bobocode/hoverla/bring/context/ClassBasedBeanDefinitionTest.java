package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.bobocode.hoverla.bring.testsubject.beandefinition.classbased.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.bobocode.hoverla.bring.helper.BeanDefinitionAssert.assertThat;
import static com.bobocode.hoverla.bring.testsubject.beandefinition.classbased.ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Disabled
class ClassBasedBeanDefinitionTest {

    @Test
    @DisplayName("Method name() returns default bean name")
    void returnDefaultBeanName() {
        // when
        final ClassBasedBeanDefinition beanDefinition = new ClassBasedBeanDefinition(ClassBasedBeanDefinitionNoName.class);

        // then
        assertThat(beanDefinition).hasName(ClassBasedBeanDefinitionNoName.class.getName());
    }

    @Test
    @DisplayName("Method name() returns provided bean name")
    void returnNameProvided() {
        // when
        final ClassBasedBeanDefinition beanDefinition = new ClassBasedBeanDefinition(ClassBasedBeanDefinitionWithName.class);

        // then
        assertThat(beanDefinition).hasName(BEAN_DEFINITION_WITH_NAME);
    }

//    @Test
//    @DisplayName("BeanDefinition instantiation of a bean with multiple constructors with @Inject annotation. Throws BeanDefinitionConstructionException")
//    void testInitBeanDefinition_MultipleConfusingConstructors_ThrowException() {
//        // given
//        final String expectedMessage = String.format("'%s' bean has multiple constructors", ClassBasedBeanDefinitionConfusingConstructors.class);
//
//        // then
//        Assertions.assertThatThrownBy(() -> new ClassBasedBeanDefinition(ClassBasedBeanDefinitionConfusingConstructors.class))
//                .isInstanceOf(BeanDefinitionConstructionException.class)
//                .hasMessage(expectedMessage);
//    }

    @Test
    @DisplayName("Return correct bean type")
    void returnType() {
        // when
        final ClassBasedBeanDefinition beanDefinition = new ClassBasedBeanDefinition(ClassBasedBeanDefinitionWithName.class);

        // then
        assertThat(beanDefinition).hasType(ClassBasedBeanDefinitionWithName.class);
    }

    @Test
    @DisplayName("Return correct bean dependencies")
    void returnTestDependencies() {
        // given
        String CLASS_BASED_BEAN_DEFINITION_ON_NAME = "classBasedBeanDefinitionNoName";
        final Map<String, Class<?>> beanDependencies = Map.of(CLASS_BASED_BEAN_DEFINITION_ON_NAME, ClassBasedBeanDefinitionNoName.class,
                ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME, ClassBasedBeanDefinitionWithName.class);

        // when
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionFieldInjection.class);

        // then
        assertThat(beanDefinition).hasDependencies(beanDependencies);
    }

    @Test
    @DisplayName("Create instance of a bean without dependencies")
    void returnBeanWithNoDependencies() {
        // given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionNoDependencies.class);

        // when
        final Object instance = beanDefinition.instance();

        // then
        assertNotNull(instance);
        assertEquals(ClassBasedBeanDefinitionNoDependencies.class, instance.getClass());
    }

    @Test
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
