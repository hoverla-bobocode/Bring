package com.bobocode.hoverla.bring.testsubject.beandefinition.classbased;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import lombok.Setter;

@Bean
@Setter
public class ClassBasedBeanDefinitionConfusingDependencies {

    @Inject
    private ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName;

    @Inject
    //@Qualifier(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME)
    private final ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName;

    private final ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies;

    @Inject
    public ClassBasedBeanDefinitionConfusingDependencies(
            @Qualifier(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME) ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName,
            ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies) {
        this.classBasedBeanDefinitionWithName = classBasedBeanDefinitionWithName;
        this.classBasedBeanDefinitionNoDependencies = classBasedBeanDefinitionNoDependencies;
    }
}