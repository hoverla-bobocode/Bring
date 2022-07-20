package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.NoSuchBeanException;
import com.bobocode.hoverla.bring.exception.NoUniqueBeanException;
import com.google.common.collect.Table;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class ApplicationContextImplTest {

    private ApplicationContext applicationContext;
    private BeanScanner beanScannerOne;
    private BeanScanner beanScannerTwo;
    private BeanValidator validator;
    private BeanInitializer initializer;

    @BeforeAll
    private void init() {
        beanScannerOne = Mockito.mock(BeanScanner.class);
        beanScannerTwo = Mockito.mock(BeanScanner.class);
        List<BeanScanner> beanScannerList = Arrays.asList(beanScannerOne, beanScannerTwo);

        validator = Mockito.mock(BeanValidator.class);

        initializer = Mockito.mock(BeanInitializer.class);

        BeanDefinition beanDefinitionStringOne = Mockito.mock(BeanDefinition.class);
        BeanDefinition beanDefinitionStringTwo = Mockito.mock(BeanDefinition.class);
        BeanDefinition beanDefinitionInteger = Mockito.mock(BeanDefinition.class);

        when(beanScannerOne.scan()).thenReturn(Arrays.asList(beanDefinitionStringOne, beanDefinitionInteger));
        when(beanScannerTwo.scan()).thenReturn(Collections.singletonList(beanDefinitionStringTwo));

        when(beanDefinitionStringOne.name()).thenReturn("String");
        doReturn(String.class).when(beanDefinitionStringOne).type();
        when(beanDefinitionStringOne.instance()).thenReturn("String bean");

        when(beanDefinitionInteger.name()).thenReturn("Integer");
        doReturn(Integer.class).when(beanDefinitionInteger).type();
        when(beanDefinitionInteger.instance()).thenReturn(2);

        when(beanDefinitionStringTwo.name()).thenReturn("One more string bean");
        doReturn(String.class).when(beanDefinitionStringTwo).type();
        when(beanDefinitionStringTwo.instance()).thenReturn("String bean");

        applicationContext = new ApplicationContextImpl(beanScannerList, validator, initializer);
    }

    @Test
    @DisplayName("Application context initializing")
    public void applicationContextInitializingTest() throws NoSuchFieldException, IllegalAccessException {
        verify(beanScannerOne).scan();
        verify(beanScannerTwo).scan();

        verify(validator).validate(anyList());

        verify(initializer).initialize(any());

        Field contextField = applicationContext.getClass().getDeclaredField("context");
        assertNotNull(contextField);
        contextField.setAccessible(true);

        Table context = (Table) contextField.getType().cast(contextField.get(applicationContext));
        assertEquals(3, context.size());
    }

    @Test
    @DisplayName("Getting a bean from the context by bean type")
    public void getBeanByType() {
        Integer bean = applicationContext.getBean(Integer.class);
        assertEquals(bean, 2);
    }

    @Test
    @DisplayName("Throwing an exception when a bean with provided type is not found")
    public void getBeanByTypeThrowsException1() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean(BigDecimal.class));
    }

    @Test
    @DisplayName("Throwing an exception when more than one bean are found")
    public void getBeanByTypeThrowsException2() {
        assertThrows(NoUniqueBeanException.class, () -> applicationContext.getBean(String.class));
    }

    @Test
    @DisplayName("Getting a bean by provided name")
    public void getBeanByName() {
        Object bean = applicationContext.getBean("String");
        assertInstanceOf(String.class, bean);
        assertEquals("String bean", (String) bean);
    }

    @Test
    @DisplayName("Throwing an exception when bean with provided name is not found")
    public void getBeanByNameThrowsException() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("Name"));
    }

    @Test
    @DisplayName("Getting a bean by provided name and type")
    public void getBeanByNameAndType() {
        String bean = applicationContext.getBean("String", String.class);
        assertNotNull(bean);
        assertEquals("String bean", bean);
    }

    @Test
    @DisplayName("Throwing an exception when bean with provided name and type is not found")
    public void getBeanByNameAndTypeThrowsException() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("Name", String.class));
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("String", Integer.class));
    }

    @Test
    @DisplayName("Getting all beans with provided type")
    public void getAllBeans() {
        Map<String, String> beans = applicationContext.getAllBeans(String.class);
        assertNotNull(beans);
        assertEquals(2, beans.size());

        String beanOne = beans.get("String");
        assertNotNull(beanOne);
        assertEquals(beanOne, "String bean");

        String beanTwo = beans.get("One more string bean");
        assertNotNull(beanTwo);
        assertEquals(beanTwo, "String bean");
    }

    @Test
    @DisplayName("Getting empty map when context does not contain bean with provided type")
    public void getEmptyMapOfBeansByProvidedType() {
        Map<String, BigDecimal> beans = applicationContext.getAllBeans(BigDecimal.class);
        assertNotNull(beans);
        assertTrue(beans.isEmpty());
    }

    @Test
    @DisplayName("Context contain bean with provided name")
    public void containsBeans() {
        assertTrue(applicationContext.containsBean("Integer"));
    }
}
