package com.bobocode.hoverla.bring.testsubject.beandefinition.classbased;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class ClassBasedBeanDefinitionMultipleConstructors {

    private ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName;

    @Inject
    private final ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName;

    private final ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies;

    @Inject
    public ClassBasedBeanDefinitionMultipleConstructors(
            ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName,
            ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName,
            ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies) {
        this.classBasedBeanDefinitionNoName = classBasedBeanDefinitionNoName;
        this.classBasedBeanDefinitionWithName = classBasedBeanDefinitionWithName;
        this.classBasedBeanDefinitionNoDependencies = classBasedBeanDefinitionNoDependencies;
    }

    public ClassBasedBeanDefinitionMultipleConstructors(
            ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName,
            ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies) {
        this.classBasedBeanDefinitionWithName = classBasedBeanDefinitionWithName;
        this.classBasedBeanDefinitionNoDependencies = classBasedBeanDefinitionNoDependencies;
    }
}
