package com.bobocode.hoverla.bring.test.subject.definition;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import lombok.Setter;

@Bean
@Setter
public class ClassBasedBeanDefinitionFieldInjection {

    @Inject
    private ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName;

    @Inject
    //@Qualifier(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME)
    private ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName;
}
