package com.bobocode.hoverla.bring.testsubject.beandefinition.classbased;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import lombok.EqualsAndHashCode;

@Bean
public class ClassBasedBeanDefinitionConstructorInjection {

    private final ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName;
    private final ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName;

    public ClassBasedBeanDefinitionConstructorInjection(
            ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName,
            ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName) {
        this.classBasedBeanDefinitionNoName = classBasedBeanDefinitionNoName;
        this.classBasedBeanDefinitionWithName = classBasedBeanDefinitionWithName;
    }
}
