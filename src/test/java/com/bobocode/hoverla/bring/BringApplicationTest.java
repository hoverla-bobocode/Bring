package com.bobocode.hoverla.bring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BringApplicationTest {
    private final String EXCEPTION_MESSAGE = "Argument [packagesToScan] must contain at least one not null and not empty element";

    @ParameterizedTest(name = "Throws exception when null or empty package name was provided")
    @NullAndEmptySource
    void throwsExceptionWhenNullOrEmptyPackages(String packageName) {
         assertThatThrownBy(() -> BringApplication.loadContext(packageName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(EXCEPTION_MESSAGE);
    }

    @DisplayName("Throws exception when no package was provided")
    @Test
    void throwsExceptionWhenNoPackages() {
        assertThatThrownBy(BringApplication::loadContext)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(EXCEPTION_MESSAGE);
    }

    @DisplayName("No exception was thrown when packages to scan were provided")
    @Test
    void providePackagesToScan() {
        assertThatNoException().isThrownBy(() -> BringApplication.loadContext("com.bobocode.hoverla.bring"));
    }

}
