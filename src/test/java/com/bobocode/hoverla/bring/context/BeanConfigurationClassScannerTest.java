package com.bobocode.hoverla.bring.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BeanConfigurationClassScannerTest {

    private static final String CONFIG_PACKAGE_NAME = "com.bobocode.hoverla.bring.test.subject.config";
    private static final String NO_CONFIG_PACKAGE_NAME = "com.bobocode.hoverla.bring.test.subject.config.empty";

    @Mock
    private BeanConfigurationClassValidator validator;

    @Mock
    private BeanDefinitionMapper mapper;

    private BeanConfigurationClassScanner scanner;

    @Test
    @DisplayName("Scans all beans from @Configuration classes in specified package")
    void scansAllBeansFromConfigClasses() {
        scanner = new BeanConfigurationClassScanner(validator, mapper, CONFIG_PACKAGE_NAME, NO_CONFIG_PACKAGE_NAME);
        List<BeanDefinition> beanDefinitions = scanner.scan();
        assertThat(beanDefinitions).hasSize(5);
    }

    @Test
    @DisplayName("Returns empty list when no config class is found")
    void returnsEmptyListWhenNoConfigFound() {
        scanner = new BeanConfigurationClassScanner(validator, mapper, NO_CONFIG_PACKAGE_NAME);

        List<BeanDefinition> beanDefinitions = scanner.scan();

        verify(validator, never()).validate(any());
        verify(mapper, never()).mapToBeanDefinition(any(), any());
        assertThat(beanDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Returns empty list when no package to scan is passed")
    void returnsEmptyListWhenNoPackageToScan() {
        scanner = new BeanConfigurationClassScanner(validator, mapper);

        List<BeanDefinition> beanDefinitions = scanner.scan();

        verify(validator, never()).validate(any());
        verify(mapper, never()).mapToBeanDefinition(any(), any());
        assertThat(beanDefinitions).isEmpty();
    }
}
