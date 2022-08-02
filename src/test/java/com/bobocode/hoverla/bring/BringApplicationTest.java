package com.bobocode.hoverla.bring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BringApplicationTest {

    @ParameterizedTest(name = "Throws exception when null or empty package name was provided")
    @NullAndEmptySource
    void throwsExceptionWhenNullOrEmptyPackages(String packageName) {
         assertThatThrownBy(() -> BringApplication.loadContext(packageName))
                 .isInstanceOf(IllegalArgumentException.class)
                 .hasMessage("Argument [packagesToScan] must not contain null or empty element");
    }

    @Test
    @DisplayName("Throws exception when no package was provided")
    void throwsExceptionWhenNoPackages() {
        assertThatThrownBy(BringApplication::loadContext)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument [packagesToScan] must contain at least one not null and not empty element");
    }

    @ParameterizedTest(name = "Throws exception when package names contain [{0}]")
    @ValueSource(strings = {"^","!","@","#","$","%","^","&","*","(",")","?","~","+","-","<",">","/",","})
    void throwsExceptionWhenInvalidPackageName(String packageName) {
        assertThatThrownBy(() -> BringApplication.loadContext(packageName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Package name must contain only letters, numbers and symbol [.]");
    }
}
