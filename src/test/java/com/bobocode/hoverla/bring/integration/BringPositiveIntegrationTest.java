package com.bobocode.hoverla.bring.integration;

import ch.qos.logback.classic.Level;
import com.bobocode.hoverla.bring.BringApplication;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BringPositiveIntegrationTest {

    private static final String PACKAGE = "com.bobocode.hoverla.bring.integration";

    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        applicationContext = BringApplication.getContextBuilder()
                .packagesToScan(PACKAGE)
                .logLevel(Level.TRACE)
                .build();
    }

    private static Stream<Arguments> fieldNamesToBeanNames() {
        return Stream.of(
                Arguments.of("testBean1FromConfig", Map.of()),
                Arguments.of("testBean2FromConfig", Map.of("testBean1", "testBean1FromConfig")),
                Arguments.of("primaryThread", Map.of()),
                Arguments.of("nonPrimaryThread", Map.of()),
                Arguments.of("nonPrimaryThread2", Map.of()),
                Arguments.of("takesInterface", Map.of("action", "primaryThread")),
                Arguments.of("takesQualifiedThread", Map.of("action1", "nonPrimaryThread", "action2", "primaryThread")),
                Arguments.of("objectBean", Map.of()),
                Arguments.of("testBean1", Map.of()),
                Arguments.of("com.bobocode.hoverla.bring.integration.beans.TestBean2", Map.of("testBean1", "testBean1")),
                Arguments.of("com.bobocode.hoverla.bring.integration.beans.TestBean3", Map.of("testBean2", "com.bobocode.hoverla.bring.integration.beans.TestBean2")),
                Arguments.of("com.bobocode.hoverla.bring.integration.beans.TestBean4", Map.of("testBean2", "testBean2FromConfig")),
                Arguments.of("com.bobocode.hoverla.bring.integration.beans.TestBean5", Map.of("action", "primaryThread")),
                Arguments.of("com.bobocode.hoverla.bring.integration.beans.TestBean6", Map.of("action1", "nonPrimaryThread", "action2", "primaryThread")),
                Arguments.of("com.bobocode.hoverla.bring.integration.beans.TestBean7", Map.of("action1", "nonPrimaryThread", "action2", "primaryThread")),
                Arguments.of("testBean8FromConfig", Map.of("testBean1FromConfig", "testBean1FromConfig")),
                Arguments.of("com.bobocode.hoverla.bring.integration.beans.TestBean10",
                        Map.of("testBean6", "com.bobocode.hoverla.bring.integration.beans.TestBean6",
                                "testBean8", "testBean8FromConfig",
                                "testBean4", "com.bobocode.hoverla.bring.integration.beans.TestBean4",
                                "testBean9", "testBean9FromConfig"))
        );
    }

    private void assertContainsBean(String beanName) {
        assertTrue(applicationContext.containsBean(beanName),
                () -> "'%s' bean is not found in application context".formatted(beanName));
    }

    @Test
    @DisplayName("Successfully loads context with all beans")
    void loadsContextWithAllBeans() {
        assertContainsBean("testBean1");
        assertContainsBean("testBean1FromConfig");
        assertContainsBean("testBean2FromConfig");
        assertContainsBean("takesInterface");
        assertContainsBean("nonPrimaryThread");
        assertContainsBean("nonPrimaryThread2");
        assertContainsBean("primaryThread");
        assertContainsBean("takesQualifiedThread");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean2");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean3");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean4");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean5");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean6");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean7");
        assertContainsBean("testBean8FromConfig");
        assertContainsBean("testBean9FromConfig");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean10");
    }

    @ParameterizedTest
    @MethodSource("fieldNamesToBeanNames")
    @DisplayName("Injects bean dependencies with expected bean instances")
    void beanInjectionWorksCorrectly(String beanName, Map<String, String> fieldNameToBeanName) {
        Object bean = applicationContext.getBean(beanName);
        fieldNameToBeanName.forEach(
                (fieldName, dependencyName) -> assertThat(bean).hasFieldOrPropertyWithValue(fieldName, applicationContext.getBean(dependencyName))
        );
    }
}
