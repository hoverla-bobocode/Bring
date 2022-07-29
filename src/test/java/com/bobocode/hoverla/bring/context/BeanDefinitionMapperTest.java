package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.support.BeanDefinitionAssert;
import com.bobocode.hoverla.bring.test.subject.config.TestBeanConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class BeanDefinitionMapperTest {

    private final BeanDefinitionMapper mapper = new BeanDefinitionMapper();

    @Test
    @DisplayName("Maps ConfigBasedBeanDefinition when bean config class and bean method are passed")
    void mapsToConfigBasedBeanDefinition() throws NoSuchMethodException {
        TestBeanConfig beanConfigClass = new TestBeanConfig();
        Method beanMethod = beanConfigClass.getClass().getMethod("beanWithNameInAnnotation");

        BeanDefinition beanDefinition = mapper.mapToBeanDefinition(beanConfigClass, beanMethod);

        BeanDefinitionAssert.assertThat(beanDefinition)
                .isInstanceOf(ConfigBasedBeanDefinition.class);
    }
}
