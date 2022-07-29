package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.bobocode.hoverla.bring.test.subject.bean.TestBeanWithInjectConstructor;
import com.bobocode.hoverla.bring.test.subject.bean.TestBeanWithInjectFields;
import com.bobocode.hoverla.bring.test.subject.bean.TestBeanWithInjectFieldsAndConstructor;
import com.bobocode.hoverla.bring.test.subject.bean.TestBeanWithPlainConstructor;
import com.bobocode.hoverla.bring.test.subject.bean.TestBeanWithoutDependencies;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean1;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean2;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean3;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean4;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean5;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.bobocode.hoverla.bring.support.BeanDefinitionAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        assertThat(beanDefinition)
                .hasDependencies(expectedDependencies);
    }

    @Test
    @DisplayName("Instantiation of bean without dependencies")
    void createBeanWithoutDependencies() {
        Class<?> beanClass = TestBeanWithoutDependencies.class;
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);

        beanDefinition.instantiate();
        Object instance = beanDefinition.getInstance();

        Assertions.assertThat(instance)
                .isNotNull()
                .isInstanceOf(beanClass);
    }

    @Test
    @DisplayName("Instantiation of bean with 1-argument @Inject constructor. Argument successfully injected")
    void createBeanWithInjectConstructor() {
        Class<?> beanClass = TestBeanWithInjectConstructor.class;
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);

        var mockInstance = new TestBeanWithoutDependencies();
        BeanDefinition dependency = prepareDefinition(TestBeanWithoutDependencies.class,
                TestBeanWithoutDependencies.class.getName(), mockInstance);

        beanDefinition.instantiate(dependency);
        Object instance = beanDefinition.getInstance();

        Assertions.assertThat(instance)
                .isNotNull()
                .isInstanceOf(beanClass)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("testBeanWithoutDependencies", mockInstance);
    }

    @Test
    @DisplayName("Instantiation of bean with 2-argument plain constructor. Arguments successfully injected")
    void createBeanWithPlainConstructor() {
        Class<?> beanClass = TestBeanWithPlainConstructor.class;
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);

        var firstMockInstance = new TestBeanWithoutDependencies();
        BeanDefinition firstDependency = prepareDefinition(TestBeanWithoutDependencies.class,
                "testBean", firstMockInstance);

        var secondMockInstance = new TestBeanWithoutDependencies();
        BeanDefinition secondDependency = prepareDefinition(TestBeanWithoutDependencies.class,
                TestBeanWithoutDependencies.class.getName(), secondMockInstance);

        beanDefinition.instantiate(firstDependency, secondDependency);
        Object instance = beanDefinition.getInstance();

        Assertions.assertThat(instance)
                .isNotNull()
                .isInstanceOf(beanClass)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("testBean1", firstMockInstance)
                .hasFieldOrPropertyWithValue("testBean2", secondMockInstance);
    }

    @Test
    @DisplayName("Instantiation of bean with 2-argument plain constructor. Constructor parameters not matched")
    void createBeanWithNotMatchedConstructorDependencies() {
        Class<?> beanClass = TestBeanWithPlainConstructor.class;
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);

        // name not matched
        BeanDefinition firstDependency = prepareDefinition(TestBeanWithoutDependencies.class, "invalidName", "ignoreVal");
        // type not matched
        BeanDefinition secondDependency = prepareDefinition(String.class, "ignoreName", "ignoreVal");

        Assertions.assertThatThrownBy(() -> beanDefinition.instantiate(firstDependency, secondDependency))
                .isInstanceOf(BeanInstanceCreationException.class)
                .hasMessageContaining("bean can't be instantiated")
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("wrong number of arguments");

    }

    @Test
    @DisplayName("Instantiation of bean with 3 @Inject fields. Fields are successfully injected")
    void createBeanWithInjectFields() {
        Class<?> beanClass = TestBeanWithInjectFields.class;
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);

        var mockInstance = new TestBeanWithoutDependencies();
        BeanDefinition firstDependency = prepareDefinition(TestBeanWithoutDependencies.class, "testBean", mockInstance);
        BeanDefinition secondDependency = prepareDefinition(Integer.class, "anInteger", 1);
        BeanDefinition thirdDependency = prepareDefinition(String.class, String.class.getName(), "strValue");

        beanDefinition.instantiate(firstDependency, secondDependency, thirdDependency);
        Object instance = beanDefinition.getInstance();

        Assertions.assertThat(instance)
                .isNotNull()
                .isInstanceOf(beanClass)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("testBean", mockInstance)
                .hasFieldOrPropertyWithValue("anInteger", 1)
                .hasFieldOrPropertyWithValue("aString", "strValue");
    }

    @Test
    @DisplayName("Instantiation of bean with 3 @Inject fields. Only 1 field matched and injected.")
    void createBeanWithNotMatchedFieldDependencies() {
        Class<?> beanClass = TestBeanWithInjectFields.class;
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);

        // name not matched
        BeanDefinition firstDependency = prepareDefinition(TestBeanWithoutDependencies.class, "invalidName", "ignoreVal");
        // type not matched
        BeanDefinition secondDependency = prepareDefinition(Byte.class, "anInteger", "ignoreVal");
        // matched
        BeanDefinition thirdDependency = prepareDefinition(String.class, String.class.getName(), "strValue");

        beanDefinition.instantiate(firstDependency, secondDependency, thirdDependency);
        Object instance = beanDefinition.getInstance();

        Assertions.assertThat(instance)
                .isNotNull()
                .isInstanceOf(beanClass)
                .hasAllNullFieldsOrPropertiesExcept("aString")
                .hasFieldOrPropertyWithValue("aString", "strValue");
    }

    @Test
    @DisplayName("Instantiation of bean with with 1-argument @Inject constructor and 2 @Inject fields. Everything successfully injected")
    void createBeanWithInjectConstructorAndFields() {
        Class<?> beanClass = TestBeanWithInjectFieldsAndConstructor.class;
        var beanDefinition = new ClassBasedBeanDefinition(beanClass);

        BeanDefinition firstDependency = prepareDefinition(String.class, String.class.getName(), "strValue");
        BeanDefinition secondDependency = prepareDefinition(Integer.class, "int", 2);
        BeanDefinition thirdDependency = prepareDefinition(Double.class, Double.class.getName(), 2.2d);

        beanDefinition.instantiate(firstDependency, secondDependency, thirdDependency);
        Object instance = beanDefinition.getInstance();

        Assertions.assertThat(instance)
                .isNotNull()
                .isInstanceOf(beanClass)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("aString", "strValue")
                .hasFieldOrPropertyWithValue("anInteger", 2)
                .hasFieldOrPropertyWithValue("aDouble", 2.2d);
    }

    private BeanDefinition prepareDefinition(Class<?> type, String name, Object instance) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(type).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(name);
        when(beanDefinition.getInstance()).thenReturn(instance);

        return beanDefinition;
    }
}
