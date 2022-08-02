package com.bobocode.hoverla.bring;

import com.bobocode.hoverla.bring.BringApplication.ApplicationContextBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.bobocode.hoverla.bring.BringApplication.getContextBuilder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationContextBuilderTest {

    @ParameterizedTest(name = "Throws exception when null or empty package name was provided")
    @NullAndEmptySource
    void throwsExceptionWhenNullOrEmptyPackages(String packageName) {
        ApplicationContextBuilder builder = getContextBuilder()
                .packagesToScan(packageName);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument [packagesToScan] must not contain null or empty element");
    }

    @Test
    @DisplayName("Throws exception when no packages were provided")
    void throwsExceptionWhenNoPackagesToScan() {
        ApplicationContextBuilder builder = getContextBuilder();

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument [packagesToScan] must contain at least one not null and not empty element");
    }

    @ParameterizedTest(name = "Throws exception when package names contain [{0}]")
    @ValueSource(strings = {"^","!","@","#","$","%","^","&","*","(",")","?","~","+","-","<",">","/",","})
    void throwsExceptionWhenInvalidPackageName(String packageName) {
        ApplicationContextBuilder builder = getContextBuilder()
                .packagesToScan(packageName);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Package name must contain only letters, numbers and symbol [.]");
    }
}
