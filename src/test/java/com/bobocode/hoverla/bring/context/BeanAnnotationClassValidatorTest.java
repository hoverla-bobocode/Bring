package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanClassValidationException;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean1;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean2;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean3;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean4;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean5;
import com.bobocode.hoverla.bring.test.subject.validation.bean.constructor.TestBeanWithInjectConstructors;
import com.bobocode.hoverla.bring.test.subject.validation.bean.constructor.TestBeanWithPlainConstructors;
import com.bobocode.hoverla.bring.test.subject.validation.bean.constructor.TestBeanWithSingleInjectConstructor;
import com.bobocode.hoverla.bring.test.subject.validation.bean.constructor.TestBeanWithoutConstructors;
import com.bobocode.hoverla.bring.test.subject.validation.bean.field.TestBeanWithFinalInjectFields;
import com.bobocode.hoverla.bring.test.subject.validation.bean.field.TestBeanWithStaticInjectFields;
import com.bobocode.hoverla.bring.test.subject.validation.bean.type.AbstractTestBean;
import com.bobocode.hoverla.bring.test.subject.validation.bean.type.EnumTestBean;
import com.bobocode.hoverla.bring.test.subject.validation.bean.type.InterfaceTestBean;
import com.bobocode.hoverla.bring.test.subject.validation.bean.type.OuterTestBean;
import com.bobocode.hoverla.bring.test.subject.validation.bean.type.RecordTestBean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeanAnnotationClassValidatorTest {

    private BeanAnnotationClassValidator validator;

    private Stream<Arguments> validBeans() {
        return Stream.of(
                Arguments.of(TestBean1.class, "validation succeeds: no invalid fields, 1 valid plain constructor"),
                Arguments.of(TestBean2.class, "validation succeeds: No invalid fields, 1 valid @Inject constructor"),
                Arguments.of(TestBean3.class, "validation succeeds: 1 valid @Inject field, 1 valid @Inject constructor"),
                Arguments.of(TestBean4.class, "validation succeeds: 2 valid @Inject fields, no invalid constructors"),
                Arguments.of(TestBean5.class, "validation succeeds: 1 valid @Inject field, 1 valid plain constructor")
        );
    }

    private Stream<Arguments> invalidBeans() {
        return Stream.of(
                Arguments.of(TestBeanWithoutConstructors.class, "Class has no public constructors"),
                Arguments.of(TestBeanWithPlainConstructors.class, "Class has 3 plain constructors. Unable to pick up one"),
                Arguments.of(TestBeanWithSingleInjectConstructor.class, "@Inject constructor has no parameters"),
                Arguments.of(TestBeanWithInjectConstructors.class, "Class has 2 constructors marked with @Inject. Unable to pick up one"),
                Arguments.of(TestBeanWithStaticInjectFields.class, "Field marked with @Inject cannot be static/final"),
                Arguments.of(TestBeanWithFinalInjectFields.class, "Field marked with @Inject cannot be static/final")
        );
    }

    private Stream<Arguments> beansWithUnsupportedType() {
        return Stream.of(
                Arguments.of(EnumTestBean.class, "validation fails: enum is marked with @Bean"),
                Arguments.of(InterfaceTestBean.class, "validation fails: interface is marked with @Bean"),
                Arguments.of(RecordTestBean.class, "validation fails: record is marked with @Bean"),
                Arguments.of(AbstractTestBean.class, "validation fails: abstract class is marked with @Bean"),
                Arguments.of(OuterTestBean.InnerTestBean.class, "validation fails: inner class is marked with @Bean"),
                Arguments.of(OuterTestBean.NestedTestBean.class, "validation fails: nested static class is marked with @Bean")
        );
    }

    @BeforeAll
    void init() {
        this.validator = new BeanAnnotationClassValidator();
    }

    @ParameterizedTest(name = "[{index}]: Positive test case - {1}")
    @MethodSource("validBeans")
    void beanValidationPositiveTest(Class<?> beanClass, String description) {
        Set<Class<?>> beanClasses = Set.of(beanClass);
        assertThatNoException()
                .isThrownBy(() -> this.validator.validateBeanClasses(beanClasses));
    }

    @ParameterizedTest(name = "[{index}]: Negative test case - {1}")
    @MethodSource("invalidBeans")
    void beanValidationNegativeTest(Class<?> beanClass, String exceptionMessage) {
        Set<Class<?>> beanClasses = Set.of(beanClass);
        assertThatThrownBy(() -> this.validator.validateBeanClasses(beanClasses))
                .isInstanceOf(BeanClassValidationException.class)
                .hasMessageContaining(exceptionMessage);
    }

    @ParameterizedTest(name = "[{index}]: Negative test case - {1}")
    @MethodSource("beansWithUnsupportedType")
    void beanTypeValidationNegativeTest(Class<?> beanClass, String description) {
        Set<Class<?>> beanClasses = Set.of(beanClass);
        assertThatThrownBy(() -> this.validator.validateBeanClasses(beanClasses))
                .isInstanceOf(BeanClassValidationException.class)
                .hasMessageContaining("Class marked as @Bean is of unsupported type");
    }

}