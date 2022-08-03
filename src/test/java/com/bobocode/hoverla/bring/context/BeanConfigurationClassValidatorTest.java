package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanConfigValidationException;
import com.bobocode.hoverla.bring.test.subject.validation.config.AbstractTestBeanConfig;
import com.bobocode.hoverla.bring.test.subject.validation.config.EnumTestBeanConfig;
import com.bobocode.hoverla.bring.test.subject.validation.config.InnerClassTestBeanConfig;
import com.bobocode.hoverla.bring.test.subject.validation.config.InterfaceTestBeanConfig;
import com.bobocode.hoverla.bring.test.subject.validation.config.InvalidMethodsTestBeanConfig;
import com.bobocode.hoverla.bring.test.subject.validation.config.RecordTestBeanConfig;
import com.bobocode.hoverla.bring.test.subject.validation.config.TestBeanConfigWithoutDefaultConstructor;
import com.bobocode.hoverla.bring.test.subject.validation.config.ValidTestBeanConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeanConfigurationClassValidatorTest {

    private final BeanConfigurationClassValidator validator = new BeanConfigurationClassValidator();

    @SneakyThrows
    private Stream<Arguments> invalidBeanConfigs() {
        return Stream.of(
                Arguments.of(
                        AbstractTestBeanConfig.class,
                        "Configuration class must not be an abstract class",
                        "Doesn't allow bean config class to be abstract"),
                Arguments.of(EnumTestBeanConfig.class,
                        "Configuration class must not be an enum",
                        "Doesn't allow bean config class to be an enum"),
                Arguments.of(InterfaceTestBeanConfig.class,
                        "Configuration class must not be an interface",
                        "Doesn't allow bean config class to be an interface"),
                Arguments.of(TestBeanConfigWithoutDefaultConstructor.class,
                        "Configuration class must have one public no-arguments constructor",
                        "Doesn't allow bean config class with no public no-arg constructor"),
                Arguments.of(Class.forName("com.bobocode.hoverla.bring.test.subject.validation.config.PackagePrivateTestBeanConfig"),
                        "Configuration class must be public",
                        "Doesn't allow non-public bean config class"),
                Arguments.of(RecordTestBeanConfig.class,
                        "Configuration class must not be a record",
                        "Doesn't allow bean config class to be a record"),
                Arguments.of(InnerClassTestBeanConfig.InnerTestBeanConfig.class,
                        "Configuration class must not have an enclosing class",
                        "Doesn't allow bean config class to be inner or nested")
        );
    }

    private Stream<Arguments> invalidBeanMethodsMessages() {
        return Stream.of(
                Arguments.of("privateMethod method must be public",
                        "Doesn't allow method marked as @Bean to be private"),
                Arguments.of(
                        "packagePrivateMethod method must be public",
                        "Doesn't allow method marked as @Bean to be package-private"),
                Arguments.of(
                        "protectedMethod method must be public",
                        "Doesn't allow method marked as @Bean to be protected"),
                Arguments.of(
                        "voidMethod method must return non-void object",
                        "Doesn't allow method marked as @Bean to be void"),
                Arguments.of(
                        "staticMethod method must not be static",
                        "Doesn't allow method marked as @Bean to be static"),
                Arguments.of("Found several method parameters with same @Qualifier value `int`",
                        "Doesn't allow method marked as @Bean to have parameters with same @Qualifier"),
                Arguments.of(
                        "Found several method parameters of type java.lang.Integer without @Qualifier",
                        "Doesn't allow method marked as @Bean to have parameters of same type without @Qualifier"
                )
        );
    }

    @Test
    @DisplayName("Correct bean configuration passes validation")
    void validateCorrectBeanConfigClass() {
        assertDoesNotThrow(() -> validator.validate(ValidTestBeanConfig.class));
    }

    @ParameterizedTest(name = "[{index}]: {2}")
    @MethodSource("invalidBeanConfigs")
    void validateInvalidBeanConfigClass(Class<?> configClass, String exceptionMsg, String description) {
        assertThatThrownBy(() -> validator.validate(configClass))
                .isInstanceOf(BeanConfigValidationException.class)
                .hasMessageContaining(exceptionMsg);
    }

    @ParameterizedTest(name = "[{index}]: {1}")
    @MethodSource("invalidBeanMethodsMessages")
    void validateInterfaceBeanConfigClass(String exceptionMsg, String description) {
        assertThatThrownBy(() -> validator.validate(InvalidMethodsTestBeanConfig.class))
                .isInstanceOf(BeanConfigValidationException.class)
                .hasMessageContaining(exceptionMsg);
    }
}
