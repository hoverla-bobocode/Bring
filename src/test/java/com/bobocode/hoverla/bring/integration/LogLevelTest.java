package com.bobocode.hoverla.bring.integration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.bobocode.hoverla.bring.BringApplication.getContextBuilder;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogLevelTest {

    @ParameterizedTest(name = "Log level [{1}] is set")
    @MethodSource("logLevelArgs")
    void setLogLevel(Level input, Level output) {
        getContextBuilder()
                .packagesToScan(this.getClass().getPackageName())
                .logLevel(input)
                .build();

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        assertThat(logger.getLevel()).isEqualTo(output);
    }

    private Stream<Arguments> logLevelArgs() {
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
