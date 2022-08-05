package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanDependencyInjectionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.bobocode.hoverla.bring.support.BeanDefinitionAssert;
import com.bobocode.hoverla.bring.test.subject.bean.TestPrimaryBean;
import com.bobocode.hoverla.bring.test.subject.config.TestBeanConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigBasedBeanDefinitionTest {

    private TestBeanConfig testBeanConfig;

    @BeforeEach
    void setUp() {
        testBeanConfig = new TestBeanConfig();
    }

    @Test
    @DisplayName("Bean name is taken from method name when there's no name specified in @Bean annotation")
    void beanWithoutNameInAnnotation() throws NoSuchMethodException {
        String methodName = "beanWithNoNameInAnnotation";
        Method method = testBeanConfig.getClass().getMethod(methodName);

        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        BeanDefinitionAssert.assertThat(beanDefinition)
                .hasName(methodName);
    }

    @Test
    @DisplayName("Bean name is taken from @Bean annotation")
    void beanWithNameInAnnotation() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("beanWithNameInAnnotation");

        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        BeanDefinitionAssert.assertThat(beanDefinition)
                .hasName("beanName");
    }

    @Test
    @DisplayName("Bean type matches method return type")
    void beanTypeMatchesMethodReturnType() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("beanWithNameInAnnotation");

        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        BeanDefinitionAssert.assertThat(beanDefinition)
                .hasType(String.class);
    }

    @Test
    @DisplayName("Bean dependencies match their parameter name or name taken from @Qualifier and type")
    void beanWithDependencies() throws NoSuchMethodException {
        String methodName = "beanWithDependencies";
        Method method = testBeanConfig.getClass().getMethod(methodName, int.class, String.class);

        BeanDefinition firstDependency = mock(BeanDefinition.class);
        when(firstDependency.getInstance()).thenReturn(1);
        BeanDefinition secondDependency = mock(BeanDefinition.class);
        when(secondDependency.getInstance()).thenReturn("string");
        BeanDependency beanDependencyInt = new BeanDependency(int.class.getName(), Integer.class, null, false);
        BeanDependency beanDependencyString = new BeanDependency("testParamName", Integer.class, null, true);
        var dependencies = Map.of(beanDependencyInt.getName(), beanDependencyInt, beanDependencyString.getName(), beanDependencyString);

        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        BeanDefinitionAssert.assertThat(beanDefinition)
                .hasDependencies(dependencies);
    }

    @Test
    @DisplayName("Fails on missed dependencies passed when instantiate() is called")
    void beanWithMissedDependencies() throws NoSuchMethodException {
        String methodName = "beanWithDependencies";
        Method method = testBeanConfig.getClass().getMethod(methodName, int.class, String.class);

        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        Assertions.assertThatThrownBy(beanDefinition::instantiate)
                .isInstanceOf(BeanInstanceCreationException.class)
                .hasMessageContaining("Bean with name 'beanWithDependencies' can't be instantiated")
                .hasRootCauseInstanceOf(BeanDependencyInjectionException.class)
                .hasStackTraceContaining("bean has no dependency that matches parameter");
    }

    @Test
    @DisplayName("Lazily instantiates beans")
    void returnsBeanInstanceCache() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("beanWithNameInAnnotation");

        ConfigBasedBeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        // Has no instance before calling instantiate()
        BeanDefinitionAssert.assertThat(beanDefinition)
                .hasFieldOrPropertyWithValue("instance", null);

        beanDefinition.instantiate();

        // Has an instance after calling instantiate()
        Object instance = beanDefinition.getInstance();
        BeanDefinitionAssert.assertThat(beanDefinition)
                .hasFieldOrPropertyWithValue("instance", instance);

        Object existingInstance = beanDefinition.getInstance();

        // Further getInstance() calls return the same object in memory
        assertThat(existingInstance).isSameAs(instance);
    }

    @Test
    void beanConstructorWithCorrectArgumentsOrder() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("beanWithDependencies", byte.class, String.class, String.class);

        BeanDefinition firstDependency = mock(BeanDefinition.class);
        when(firstDependency.getInstance()).thenReturn((byte) 1);
        when(firstDependency.name()).thenReturn(byte.class.getName());
        doReturn(byte.class).when(firstDependency).type();

        BeanDefinition secondDependency = mock(BeanDefinition.class);
        when(secondDependency.getInstance()).thenReturn("string");
        when(secondDependency.name()).thenReturn("testParam");
        doReturn(String.class).when(secondDependency).type();

        BeanDefinition thirdDependency = mock(BeanDefinition.class);
        when(thirdDependency.getInstance()).thenReturn("anotherString");
        when(thirdDependency.name()).thenReturn(String.class.getName());
        doReturn(String.class).when(thirdDependency).type();

        ConfigBasedBeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        // dependencies are shuffled - should still invoke method with correct order
        assertThatNoException()
                .isThrownBy(() -> beanDefinition.instantiate(thirdDependency, secondDependency, firstDependency));

        Object instance = beanDefinition.getInstance();

        assertThat(instance)
                .isNotNull()
                .isInstanceOf(Byte.class)
                .isEqualTo((byte) 0);
    }

    @Test
    @DisplayName("Method marked as primary is treated as a primary bean definitions")
    void primaryBeansTest() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("primaryBean");
        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(TestPrimaryBean.class, method);
        BeanDefinitionAssert.assertThat(beanDefinition).isPrimary();
    }

    @Test
    @DisplayName("Method not marked as primary is not treated as a primary bean definitions")
    void nonPrimaryBeansTest() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("beanWithNameInAnnotation");
        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(TestPrimaryBean.class, method);
        BeanDefinitionAssert.assertThat(beanDefinition).isNotPrimary();
    }
}
