package com.bobocode.hoverla.bring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BringApplicationTest {
    private final String PACKAGE = "com.bobocode.hoverla.bring";

    @DisplayName("Throws IllegalArgumentException: no packages were provided")
    @Test
    void throwsExceptionPackageToScanIsEmpty() {
        String exceptionMessage = "Argument [packagesToScan] must contain at least one element";
        assertThatThrownBy(() -> BringApplication.getContextBuilder().build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(exceptionMessage);

        assertThatThrownBy(BringApplication::loadContext)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(exceptionMessage);
    }

    @DisplayName("No exception was thrown when class was provided")
    @Test
    void provideUserClassOnly() {
        assertThatNoException().isThrownBy(() -> BringApplication.loadContext(this.getClass()));

        assertThatNoException().isThrownBy(() -> BringApplication.getContextBuilder()
                .classToScan(this.getClass())
                .build());
    }

    @DisplayName("No exception was thrown when packages to scan were provided")
    @Test
    void providePackagesToScan() {
        assertThatNoException().isThrownBy(() -> BringApplication.loadContext(PACKAGE));

        assertThatNoException().isThrownBy(() -> BringApplication.getContextBuilder()
                .packagesToScan(PACKAGE)
                .build());
    }

    @DisplayName("No exception was thrown when both packages and user class were provided")
    @Test
    void providePackagesToScanAndUserClass() {
        assertThatNoException().isThrownBy(() -> BringApplication.getContextBuilder()
                .packagesToScan(PACKAGE)
                .classToScan(this.getClass())
                .build());
    }

    @DisplayName("Log level DEBUG is set")
    @Test
    void setLogLevel() {
        ApplicationContext context = BringApplication.getContextBuilder()
                .packagesToScan(PACKAGE)
                .logLevel(Level.DEBUG)
                .build();

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        assertThat(logger.getLevel()).isEqualTo(Level.DEBUG);

    }
}
