package com.bobocode.hoverla.bring.integration;

import ch.qos.logback.classic.Level;
import com.bobocode.hoverla.bring.BringApplication;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BringNegativeIntegrationTest {

    private static final String PACKAGE = "com.bobocode.hoverla.bring.integration";

    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        applicationContext = BringApplication.getContextBuilder()
                .packagesToScan(PACKAGE)
                .logLevel(Level.TRACE)
                .build();
    }

    private static Stream<Arguments> beanNames() {
        return Stream.of(
                Arguments.of("testBean9FromConfig")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = "testBean9FromConfig")
    @DisplayName("Bean dependencies were not injected")
    void beanInjectionValidatesCorrectly(String beanName) {
        Object bean = applicationContext.getBean(beanName);
        assertThat(bean).hasAllNullFieldsOrProperties();
    }
}