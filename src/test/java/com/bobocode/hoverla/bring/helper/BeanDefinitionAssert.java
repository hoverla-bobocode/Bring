package com.bobocode.hoverla.bring.helper;

import com.bobocode.hoverla.bring.context.BeanDefinition;
import org.assertj.core.api.AbstractObjectAssert;

import java.util.Map;
import java.util.Objects;

/**
 * Class to help assert test cases related to BeanDefinition
 */
public class BeanDefinitionAssert extends AbstractObjectAssert<BeanDefinitionAssert, BeanDefinition> {
    public BeanDefinitionAssert(BeanDefinition actual) {
        super(actual, BeanDefinitionAssert.class);
    }

    public static BeanDefinitionAssert assertThat(BeanDefinition actual) {
        return new BeanDefinitionAssert(actual);
    }

    public BeanDefinitionAssert hasName(String name) {
        isNotNull();

        if (!Objects.equals(actual.name(), name)) {
            failWithMessage("Expected bean name to be '%s' but was '%s'", name, actual.name());
        }

        return this;
    }

    public BeanDefinitionAssert hasType(Class<?> type) {
        isNotNull();

        if (!Objects.equals(actual.type(), type)) {
            failWithMessage("Expected bean type to be '%s' but was '%s'", type, actual.type());
        }

        return this;
    }

    public BeanDefinitionAssert hasDependencies(Map<String, Class<?>> dependencies) {
        isNotNull();

        if (!Objects.equals(actual.dependencies(), dependencies)) {
            failWithMessage("Expected bean dependencies to be '%s' but was '%s'", dependencies, actual.dependencies());
        }

        return this;
    }
}
