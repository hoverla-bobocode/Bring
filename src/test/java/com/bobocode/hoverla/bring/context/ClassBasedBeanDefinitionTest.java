package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.exception.BeanDefinitionConstructionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.bobocode.hoverla.bring.testsubject.beandefinition.classbased.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.bobocode.hoverla.bring.helper.BeanDefinitionAssert.assertThat;
import static com.bobocode.hoverla.bring.testsubject.beandefinition.classbased.ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ClassBasedBeanDefinitionTest {

    @Test
    @DisplayName("Method name() returns default bean name")
    void testName_ReturnDefaultBeanName() {
//        when
        final ClassBasedBeanDefinition beanDefinition = new ClassBasedBeanDefinition(ClassBasedBeanDefinitionNoName.class);

//        then
        assertThat(beanDefinition).hasName(ClassBasedBeanDefinitionNoName.class.getSimpleName());
    }

    @Test
    @DisplayName("Method name() returns provided bean name")
    void testName_ReturnNameProvided() {
//        when
        final ClassBasedBeanDefinition beanDefinition = new ClassBasedBeanDefinition(ClassBasedBeanDefinitionWithName.class);

//        then
        assertThat(beanDefinition).hasName(BEAN_DEFINITION_WITH_NAME);
    }

    @Test
    @DisplayName("BeanDefinition instantiation of a class without @Bean annotation. Throws BeanDefinitionConstructionException")
    void testInitBeanDefinition_ThrowException() {
//        given
        final String expectedMessage = String.format("'%s' bean is not marked as @%s",
                ClassBasedBeanDefinitionNoAnnotation.class, Bean.class.getSimpleName());

//        then
        Assertions.assertThatThrownBy(() -> new ClassBasedBeanDefinition(ClassBasedBeanDefinitionNoAnnotation.class))
                .isInstanceOf(BeanDefinitionConstructionException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    @DisplayName("BeanDefinition instantiation of a bean with multiple constructors with @Inject annotation. Throws BeanDefinitionConstructionException")
    void testInitBeanDefinition_MultipleConfusingConstructors_ThrowException() {
//        given
        final String expectedMessage = String.format("'%s' bean has multiple constructors marked as @%s",
                ClassBasedBeanDefinitionConfusingConstructors.class, Inject.class.getSimpleName());

//        then
        Assertions.assertThatThrownBy(() -> new ClassBasedBeanDefinition(ClassBasedBeanDefinitionConfusingConstructors.class))
                .isInstanceOf(BeanDefinitionConstructionException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    @DisplayName("Return correct bean type")
    void testType() {
//        when
        final ClassBasedBeanDefinition beanDefinition = new ClassBasedBeanDefinition(ClassBasedBeanDefinitionWithName.class);

//        then
        assertThat(beanDefinition).hasType(ClassBasedBeanDefinitionWithName.class);
    }

    @Test
    @DisplayName("Return correct bean dependencies")
    void testDependencies() {
//        given
        final Map<String, Class<?>> beanDependencies = Map.of(ClassBasedBeanDefinitionNoName.class.getSimpleName(), ClassBasedBeanDefinitionNoName.class,
                ClassBasedBeanDefinitionWithName.class.getSimpleName(), ClassBasedBeanDefinitionWithName.class);

//        when
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionFieldInjection.class);

//        then
        assertThat(beanDefinition).hasDependencies(beanDependencies);
    }

    @Test
    @DisplayName("Create instance of a bean without dependencies")
    void testInstance_ReturnBeanWithNoDependencies() {
//        given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionNoDependencies.class);

//        when
        final Object instance = beanDefinition.instance();

//        then
        assertNotNull(instance);
        assertEquals(ClassBasedBeanDefinitionNoDependencies.class, instance.getClass());
        assertEquals(new ClassBasedBeanDefinitionNoDependencies(), instance);
    }

    @Test
    @DisplayName("Create instance of a bean with constructor injection")
    void testInstance_ReturnBeanWithConstructorInjection() {
//        given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionConstructorInjection.class);

        final ClassBasedBeanDefinitionNoName dependencyInstance1 = new ClassBasedBeanDefinitionNoName();
        final ClassBasedBeanDefinitionWithName dependencyInstance2 = new ClassBasedBeanDefinitionWithName();
        final ClassBasedBeanDefinitionConstructorInjection expectedBean =
                new ClassBasedBeanDefinitionConstructorInjection(dependencyInstance1, dependencyInstance2);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);

//        when
        final Object instance = beanDefinition.instance(dependency1, dependency2);

//        then
        assertNotNull(instance);
        assertEquals(expectedBean, instance);

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
    }

    @Test
    @DisplayName("Create instance of a bean with field injection")
    void testInstance_ReturnBeanWithFieldInjection() {
//        given
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

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);

//        when
        final Object instance = beanDefinition.instance(dependency1, dependency2);

//        then
        assertNotNull(instance);
        assertEquals(expectedBean, instance);

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
    }

    @Test
    @DisplayName("Create instance of a bean with constructor and field injection")
    void testInstance_ReturnBeanWithConstructorAndFieldInjection() {
//        given
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

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);
        when(dependency3.instance()).thenReturn(dependencyInstance3);

//        when
        final Object instance = beanDefinition.instance(dependency1, dependency2, dependency3);

//        then
        assertNotNull(instance);
        assertEquals(expectedBean, instance);

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
        verify(dependency3).instance(any());
    }

    @Test
    @DisplayName("Create bean instance. Dependencies return null. Throws BeanInstanceCreationException")
    void testInstance_ThrowBeanInstanceCreationException() {
//        given
        final String expectedMessage = "'%s' bean can't be instantiated".formatted(ClassBasedBeanDefinitionFieldInjection.class);
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionFieldInjection.class);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.instance()).thenReturn(null);
        when(dependency2.instance()).thenReturn(null);

//        then
        Assertions.assertThatThrownBy(
                        () -> beanDefinition.instance(dependency1, dependency2))
                .isInstanceOf(BeanInstanceCreationException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    @DisplayName("Create bean instance. Parameters are nulls. Throws BeanInstanceCreationException")
    void testInstance_NullParameter_ThrowBeanInstanceCreationException() {
        // given
        final String expectedMessage = "'%s' bean can't be instantiated".formatted(ClassBasedBeanDefinitionFieldInjection.class);
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionFieldInjection.class);

        // then
        Assertions.assertThatThrownBy(
                        () -> beanDefinition.instance(null, null))
                .isInstanceOf(BeanInstanceCreationException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    @DisplayName("Create instance of a bean with confusing dependencies")
    void testInstance_ReturnBeanWithConfusingDependencies() {
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

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);
        when(dependency3.instance()).thenReturn(dependencyInstance3);

//        when
        final Object instance = beanDefinition.instance(dependency1, dependency2, dependency3);

//        then
        assertNotNull(instance);
        assertEquals(expectedBean, instance);

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
        verify(dependency3).instance(any());
    }

    @Test
    @DisplayName("Create instance of a bean with multiple constructors")
    void testInstance_ReturnBeanWithMultipleConstructors() {
//        given
        final ClassBasedBeanDefinition beanDefinition =
                new ClassBasedBeanDefinition(ClassBasedBeanDefinitionMultipleConstructors.class);

        final ClassBasedBeanDefinitionNoName dependencyInstance1 = new ClassBasedBeanDefinitionNoName();
        final ClassBasedBeanDefinitionWithName dependencyInstance2 = new ClassBasedBeanDefinitionWithName();
        final ClassBasedBeanDefinitionNoDependencies dependencyInstance3 = new ClassBasedBeanDefinitionNoDependencies();
        final ClassBasedBeanDefinitionConfusingDependencies expectedBean =
                new ClassBasedBeanDefinitionConfusingDependencies(dependencyInstance2, dependencyInstance3);
        expectedBean.setClassBasedBeanDefinitionNoName(dependencyInstance1);

        final ClassBasedBeanDefinition dependency1 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency2 = mock(ClassBasedBeanDefinition.class);
        final ClassBasedBeanDefinition dependency3 = mock(ClassBasedBeanDefinition.class);

        when(dependency1.instance()).thenReturn(dependencyInstance1);
        when(dependency2.instance()).thenReturn(dependencyInstance2);
        when(dependency3.instance()).thenReturn(dependencyInstance3);

//        when
        final Object instance = beanDefinition.instance(dependency1, dependency2, dependency3);

//        then
        assertNotNull(instance);
        assertEquals(expectedBean, instance);

        verify(dependency1).instance(any());
        verify(dependency2).instance(any());
        verify(dependency3).instance(any());
    }
}
