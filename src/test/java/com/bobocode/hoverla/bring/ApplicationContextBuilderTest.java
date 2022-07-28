package com.bobocode.hoverla.bring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationContextBuilderTest {

    private final String PACKAGE = "com.bobocode.hoverla.bring";
    private final String EXCEPTION_MESSAGE = "Argument [packagesToScan] must contain at least one not null and not empty element";

    @ParameterizedTest(name = "Throws exception when null or empty package name was provided")
    @NullAndEmptySource
    void throwsExceptionWhenNullOrEmptyPackages(String packageName) {
        var builder =  BringApplication.getContextBuilder()
                .packagesToScan(packageName);
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(EXCEPTION_MESSAGE);
    }

    @DisplayName("Throws exception when no packages were provided")
    @Test
    void throwsExceptionWhenNoPackagesToScan() {
        var builder =  BringApplication.getContextBuilder();
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(EXCEPTION_MESSAGE);
    }

    @DisplayName("No exception was thrown when packages to scan were provided")
    @Test
    void providePackagesToScan() {
        assertThatNoException().isThrownBy(() -> BringApplication.getContextBuilder()
                .packagesToScan(PACKAGE)
                .build());
    }


    @ParameterizedTest(name = "Log level [{1}] is set")
    @MethodSource
    void setLogLevel(Level input, Level output) {
        BringApplication.getContextBuilder()
                .packagesToScan(PACKAGE)
                .logLevel(input)
                .build();

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        assertThat(logger.getLevel()).isEqualTo(output);

    }

    private Stream<Arguments> setLogLevel() {
        return Stream.of(
                Arguments.of(Level.OFF, Level.OFF),
                Arguments.of(Level.ERROR, Level.ERROR),
                Arguments.of(Level.WARN, Level.WARN),
                Arguments.of(Level.INFO, Level.INFO),
                Arguments.of(Level.DEBUG, Level.DEBUG),
                Arguments.of(Level.TRACE, Level.TRACE),
                Arguments.of(Level.ALL, Level.ALL)
        );
    }
}
