package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.testsubject.config.TestBeanConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class BeanDefinitionMapperTest {

    private BeanDefinitionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BeanDefinitionMapper();
    }

    @Test
    @DisplayName("Can map to ConfigBasedBeanDefinition passing bean config class and bean method")
    void mapsToConfigBasedBeanDefinition() throws NoSuchMethodException {
        TestBeanConfig beanConfigClass = new TestBeanConfig();
        Method beanMethod = beanConfigClass.getClass().getMethod("beanWithNameInAnnotation");

        BeanDefinition beanDefinition = mapper.mapToBeanDefinition(beanConfigClass, beanMethod);

        Assertions.assertThat(beanDefinition).isInstanceOf(ConfigBasedBeanDefinition.class);
    }
}