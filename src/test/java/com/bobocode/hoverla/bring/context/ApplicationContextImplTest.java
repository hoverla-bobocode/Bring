package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.NoSuchBeanException;
import com.bobocode.hoverla.bring.exception.NoUniqueBeanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationContextImplTest {

    private static ApplicationContext applicationContext;
    private static BeanScanner beanScannerOne;
    private static BeanScanner beanScannerTwo;
    private static BeanDefinitionValidator validator;
    private static BeanInitializer initializer;

    @BeforeEach
    void init() {
        beanScannerOne = Mockito.mock(BeanScanner.class);
        beanScannerTwo = Mockito.mock(BeanScanner.class);
        List<BeanScanner> beanScannerList = Arrays.asList(beanScannerOne, beanScannerTwo);

        validator = Mockito.mock(BeanDefinitionValidator.class);

        initializer = Mockito.mock(BeanInitializer.class);

        BeanDefinition beanDefinitionStringOne = Mockito.mock(BeanDefinition.class);
        BeanDefinition beanDefinitionStringTwo = Mockito.mock(BeanDefinition.class);
        BeanDefinition beanDefinitionInteger = Mockito.mock(BeanDefinition.class);

        when(beanScannerOne.scan()).thenReturn(Arrays.asList(beanDefinitionStringOne, beanDefinitionInteger));
        when(beanScannerTwo.scan()).thenReturn(Collections.singletonList(beanDefinitionStringTwo));

        when(beanDefinitionStringOne.name()).thenReturn("String");
        doReturn(String.class).when(beanDefinitionStringOne).type();
        when(beanDefinitionStringOne.getInstance()).thenReturn("String bean");

        when(beanDefinitionInteger.name()).thenReturn("Integer");
        doReturn(Integer.class).when(beanDefinitionInteger).type();
        when(beanDefinitionInteger.getInstance()).thenReturn(2);

        when(beanDefinitionStringTwo.name()).thenReturn("One more string bean");
        doReturn(String.class).when(beanDefinitionStringTwo).type();
        when(beanDefinitionStringTwo.getInstance()).thenReturn("String bean");

        applicationContext = new ApplicationContextImpl(beanScannerList, validator, initializer);
    }

    @Test
    @DisplayName("Calls bean scanners, context validator and bean initializer during on creation")
    void applicationContextInitializingTest() {
        verify(beanScannerOne).scan();
        verify(beanScannerTwo).scan();

        verify(validator).validate(anyList());

        verify(initializer).initialize(any());
    }

    @Test
    @DisplayName("Getting a bean from the context by bean type")
    void getBeanByType() {
        Integer bean = applicationContext.getBean(Integer.class);
        assertEquals(2, bean);
    }

    @Test
    @DisplayName("Throwing an exception when a bean with provided type is not found")
    void getBeanByTypeThrowsNoSuchBeanException() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean(BigDecimal.class));
    }

    @Test
    @DisplayName("Throwing an exception when more than one bean are found")
    void getBeanByTypeThrowsNoUniqueBeanException() {
        assertThrows(NoUniqueBeanException.class, () -> applicationContext.getBean(String.class));
    }

    @Test
    @DisplayName("Getting a bean by provided name")
    void getBeanByName() {
        Object bean = applicationContext.getBean("String");
        assertInstanceOf(String.class, bean);
        assertEquals("String bean", bean);
    }

    @Test
    @DisplayName("Throwing an exception when bean with provided name is not found")
    void getBeanByNameThrowsException() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("Name"));
    }

    @Test
    @DisplayName("Getting a bean by provided name and type")
    void getBeanByNameAndType() {
        String bean = applicationContext.getBean("String", String.class);
        assertNotNull(bean);
        assertEquals("String bean", bean);
    }

    @Test
    @DisplayName("Throwing an exception when bean with provided name and type is not found")
    void getBeanByNameAndTypeThrowsException() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("Name", String.class));
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("String", Integer.class));
    }

    @Test
    @DisplayName("Getting all beans with provided type")
    void getAllBeans() {
        Map<String, String> beans = applicationContext.getAllBeans(String.class);
        assertNotNull(beans);
        assertEquals(2, beans.size());

        String beanOne = beans.get("String");
        assertNotNull(beanOne);
        assertEquals("String bean", beanOne);

        String beanTwo = beans.get("One more string bean");
        assertNotNull(beanTwo);
        assertEquals("String bean", beanTwo);
    }

    @Test
    @DisplayName("Getting empty map when context does not contain bean with provided type")
    void getEmptyMapOfBeansByProvidedType() {
        Map<String, BigDecimal> beans = applicationContext.getAllBeans(BigDecimal.class);
        assertNotNull(beans);
        assertTrue(beans.isEmpty());
    }

    @Test
    @DisplayName("Context contain bean with provided name")
    void containsBeans() {
        assertTrue(applicationContext.containsBean("Integer"));
    }
}
