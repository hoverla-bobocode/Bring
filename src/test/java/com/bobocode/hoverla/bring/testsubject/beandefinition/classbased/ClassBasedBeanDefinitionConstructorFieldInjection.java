package com.bobocode.hoverla.bring.testsubject.beandefinition.classbased;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import lombok.Getter;
import lombok.Setter;

@Bean
@Getter
@Setter
public class ClassBasedBeanDefinitionConstructorFieldInjection {

    @Inject
    private ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName;

    private final ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName;

    private final ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies;

    @Inject
    public ClassBasedBeanDefinitionConstructorFieldInjection(
            @Qualifier(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME) ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName,
            ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies) {
        this.classBasedBeanDefinitionWithName = classBasedBeanDefinitionWithName;
        this.classBasedBeanDefinitionNoDependencies = classBasedBeanDefinitionNoDependencies;
    }
}
