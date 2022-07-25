package com.bobocode.hoverla.bring.testsubject.beandefinition.classbased;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Bean
@Getter
@Setter
@EqualsAndHashCode
public class ClassBasedBeanDefinitionConstructorFieldInjection {

    @Inject
    private ClassBasedBeanDefinitionNoName classBasedBeanDefinitionNoName;

    private final ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName;

    private final ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies;

    public ClassBasedBeanDefinitionConstructorFieldInjection(
            @Qualifier(ClassBasedBeanDefinitionWithName.BEAN_DEFINITION_WITH_NAME) ClassBasedBeanDefinitionWithName classBasedBeanDefinitionWithName,
            ClassBasedBeanDefinitionNoDependencies classBasedBeanDefinitionNoDependencies) {
        this.classBasedBeanDefinitionWithName = classBasedBeanDefinitionWithName;
        this.classBasedBeanDefinitionNoDependencies = classBasedBeanDefinitionNoDependencies;
    }
}
