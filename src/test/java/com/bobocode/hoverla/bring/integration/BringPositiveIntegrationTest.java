package com.bobocode.hoverla.bring.integration;

import com.bobocode.hoverla.bring.BringApplication;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BringPositiveIntegrationTest {

    private static final String PACKAGE = "com.bobocode.hoverla.bring.integration";

    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        applicationContext = BringApplication.loadContext(PACKAGE);
    }

    @Test
    @DisplayName("Successfully loads context with all beans")
    void loadsContextWithAllBeans() {
        assertContainsBean("testBean1");
        assertContainsBean("testBean1FromConfig");
        assertContainsBean("testBean2FromConfig");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean2");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean3");
        assertContainsBean("com.bobocode.hoverla.bring.integration.beans.TestBean4");
    }

    private void assertContainsBean(String beanName) {
        assertTrue(applicationContext.containsBean(beanName),
                () -> "'%s' bean is not found in applicaion context".formatted(beanName));
    }
}
