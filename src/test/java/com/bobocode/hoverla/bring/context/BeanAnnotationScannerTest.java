package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean1;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean2;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean3;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean4;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean5;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeanAnnotationScannerTest {

    private static final String TEST_PACKAGE_TO_SCAN = "com.bobocode.hoverla.bring.test.subject.bean.util";
    private static final String EMPTY_PACKAGE_TO_SCAN = "com.bobocode.hoverla.bring.test.subject.empty";

    @Mock
    private BeanAnnotationClassValidator validator;

    @Mock
    private BeanDefinitionMapper mapper;

    @Test
    @DisplayName("Scans all beans from a given package, validates those classes and maps them to bean definitions")
    void testScan() {
        Set<Class<?>> expectedBeanClasses = Set.of(TestBean1.class, TestBean2.class, TestBean3.class,
                TestBean4.class, TestBean5.class);
        for (Class<?> beanClass : expectedBeanClasses) {
            BeanDefinition expectedBD = prepareBeanDefinition(beanClass);
            when(mapper.mapToBeanDefinition(beanClass)).thenReturn(expectedBD);
        }

        var beanAnnotationScanner = new BeanAnnotationScanner(validator, mapper, TEST_PACKAGE_TO_SCAN);
        List<BeanDefinition> scannedDefinitions = beanAnnotationScanner.scan();

        verify(validator).validateBeanClasses(anySet());
        verify(mapper, times(5)).mapToBeanDefinition(any(Class.class));

        assertThat(scannedDefinitions)
                .hasSameSizeAs(expectedBeanClasses)
                .allSatisfy(bd -> assertThat(bd).isInstanceOf(BeanDefinition.class))
                .allSatisfy(bd -> assertThat(bd.type()).isIn(expectedBeanClasses));
    }

    @Test
    @DisplayName("Scan returns empty collection, since there are no classes marked with @Bean. Validation and mapping don't happen")
    void testScanWithoutBeans() {
        var beanAnnotationScanner = new BeanAnnotationScanner(validator, mapper, EMPTY_PACKAGE_TO_SCAN);
        List<BeanDefinition> scannedDefinitions = beanAnnotationScanner.scan();

        verify(validator, never()).validateBeanClasses(anySet());
        verify(mapper, never()).mapToBeanDefinition(any(Class.class));

        assertThat(scannedDefinitions).isEmpty();
    }

    private BeanDefinition prepareBeanDefinition(Class<?> beanClass) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(beanClass).when(beanDefinition).type();
        return beanDefinition;
    }
}