package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanDefinitionConstructionException;
import com.bobocode.hoverla.bring.exception.BeanInstanceCreationException;
import com.bobocode.hoverla.bring.helper.BeanDefinitionAssert;
import com.bobocode.hoverla.bring.testsubject.NotMarkedTestConfig;
import com.bobocode.hoverla.bring.testsubject.TestBeanConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

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
        when(firstDependency.instance()).thenReturn(1);
        BeanDefinition secondDependency = mock(BeanDefinition.class);
        when(secondDependency.instance()).thenReturn("string");
        Map<String, Class<?>> dependencies = Map.of("num", int.class, "testParamName", String.class);

        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        BeanDefinitionAssert.assertThat(beanDefinition)
                            .hasDependencies(dependencies);
    }

    @Test
    @DisplayName("Fails on missed dependencies passed when instance() is called")
    void beanWithMissedDependencies() throws NoSuchMethodException {
        String methodName = "beanWithDependencies";
        Method method = testBeanConfig.getClass().getMethod(methodName, int.class, String.class);

        BeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        Assertions.assertThatThrownBy(beanDefinition::instance)
                  .isInstanceOf(BeanInstanceCreationException.class)
                  .hasMessageContaining("'beanWithDependencies' bean can't be instantiated")
                  .hasStackTraceContaining("wrong number of arguments");
    }

    @Test
    @DisplayName("Fails on configuration class not marked as @Configuration")
    void configurationClassWithNoAnnotation() throws NoSuchMethodException {
        NotMarkedTestConfig configInstance = new NotMarkedTestConfig();
        Method method = configInstance.getClass().getMethod("bean");

        Assertions.assertThatThrownBy(() -> new ConfigBasedBeanDefinition(configInstance, method))
                  .isInstanceOf(BeanDefinitionConstructionException.class)
                  .hasMessageContaining("Configuration class instance passed is not marked as @Configuration");
    }

    @Test
    @DisplayName("Fails on method not marked as @Bean")
    void methodNotMarkedAsBean() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("notBeanMethod");

        Assertions.assertThatThrownBy(() -> new ConfigBasedBeanDefinition(testBeanConfig, method))
                  .isInstanceOf(BeanDefinitionConstructionException.class)
                  .hasMessageContaining("Configuration method to create bean is not marked as @Bean");
    }

    @Test
    @DisplayName("Lazily instantiates beans")
    void returnsBeanInstanceCache() throws NoSuchMethodException {
        Method method = testBeanConfig.getClass().getMethod("beanWithNameInAnnotation");

        ConfigBasedBeanDefinition beanDefinition = new ConfigBasedBeanDefinition(testBeanConfig, method);

        // Has no instance before calling instance()
        BeanDefinitionAssert.assertThat(beanDefinition)
                            .hasFieldOrPropertyWithValue("instance", null);

        Object instance = beanDefinition.instance();

        // Has an instance after calling instance()
         BeanDefinitionAssert.assertThat(beanDefinition)
                            .hasFieldOrPropertyWithValue("instance", instance);

        Object cachedInstance = beanDefinition.instance();

        // Further instance() calls return the same object in memory
        Assertions.assertThat(cachedInstance).isSameAs(instance);
    }
}
