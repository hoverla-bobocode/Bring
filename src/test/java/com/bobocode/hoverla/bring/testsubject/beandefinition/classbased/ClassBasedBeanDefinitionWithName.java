package com.bobocode.hoverla.bring.testsubject.beandefinition.classbased;

import com.bobocode.hoverla.bring.annotation.Bean;
import lombok.EqualsAndHashCode;

@Bean(name = ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME)
@EqualsAndHashCode
public class ClassBasedBeanDefinitionWithName {

    public static final String BEAN_DEFINITION_WITH_NAME = "beanDefinitionWithName";
}
