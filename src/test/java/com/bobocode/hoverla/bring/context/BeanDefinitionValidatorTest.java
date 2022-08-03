package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.BeanValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BeanDefinitionValidatorTest {

    private static final String BD1 = "beanDef1";
    private static final String BD2 = "beanDef2";
    private static final String BD3 = "beanDef3";
    private static final String BD4 = "beanDef4";
    private List<BeanDefinition> beanDefinitionList;
    private BeanDefinitionValidator beanDefinitionValidator;

    @BeforeEach
    public void setUp() {
        beanDefinitionValidator = new BeanDefinitionValidator();
    }

    @Test
    @DisplayName("All validations passed successfully")
    void validationWithoutErrors() {
        BeanDefinition beanDef4 = prepareDefinition(BD4, Integer.class);
        BeanDefinition beanDef3 = prepareDefinition(BD3, Byte.class, beanDef4);
        BeanDefinition beanDef2 = prepareDefinition(BD2, Short.class, beanDef3, beanDef4);
        BeanDefinition beanDef1 = prepareDefinition(BD1, String.class, beanDef2, beanDef3);
        beanDefinitionList = List.of(beanDef1, beanDef2, beanDef3, beanDef4);

        assertDoesNotThrow(() -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when bean definition list is null")
    void validationWhenListIsNull() {
        String expectedMessage = "Bean definition list is null";
        assertThatThrownBy(() -> beanDefinitionValidator.validate(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage(expectedMessage);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "with space", "name\n", "tab\namenamer"})
    @DisplayName("Fails on name validation for bean when it's empty, blank or contains space, new line, carriage return or tab symbols")
    void beanNameIsIncorrect(String name) {
        String expectedMessage = "Bean of type java.lang.String has invalid name";
        BeanDefinition beanDefinition = prepareDefinition(name, String.class);
        beanDefinitionList = List.of(beanDefinition);

        assertThatThrownBy(() -> beanDefinitionValidator.validate(beanDefinitionList))
                .isInstanceOf(BeanValidationException.class)
                .hasMessageContaining(expectedMessage);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "with space", "name\n", "tab\namenamer"})
    @DisplayName("Fails on dependency name validation when it's empty, blank or contains space, new line, carriage return or tab symbols")
    void dependencyNameIsIncorrect(String name) {
        String expectedMessage = "Bean `beanDef1` has dependency with invalid name";
        BeanDefinition beanDefinition = prepareDefinition(BD1, String.class);
        beanDefinition.dependencies().put(name, Integer.class);
        beanDefinitionList = List.of(beanDefinition);

        assertThatThrownBy(() -> beanDefinitionValidator.validate(beanDefinitionList))
                .isInstanceOf(BeanValidationException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    @DisplayName("Fails on dependency validation when found more than 1 dependency with the same name")
    void duplicateNamesInDependencies() {
        String expectedMessage = "Context contains beans with duplicate names: [beanDef1]";
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        BeanDefinition beanDef2 = prepareDefinition(BD1, String.class);
        beanDefinitionList = List.of(beanDef1, beanDef2);

        assertExceptionAndMessage(expectedMessage, () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails on dependency validation when the found dependency type is not suitable for the required type")
    void unsuitableTypeForDependency() {
        String expectedMessage = "Found dependency type is not assignable from the required type - required is %s but found %s";
        BeanDefinition beanDef1 = prepareDefinition("anotherType", String.class);
        BeanDefinition beanDefinitionWithConflictDependencies = prepareDefinition(BD2, String.class, beanDef1);
        beanDefinitionList = List.of(beanDef1, beanDefinitionWithConflictDependencies);

        doReturn(Integer.class).when(beanDef1).type();

        assertExceptionAndMessage(
                expectedMessage.formatted(String.class.getName(), Integer.class.getName()),
                () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when beans have a circular dependency: A -> B -> C -> A")
    void circularDependency() {
        String expectedMessage = """
                Oops. Circular dependency occurs with bean: beanDef3 - java.lang.Long
                beanDef1 depends on: [beanDef2]
                beanDef2 depends on: [beanDef3]
                beanDef3 depends on: [beanDef1]""";
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        BeanDefinition beanDef2 = prepareDefinition(BD2, String.class);
        BeanDefinition beanDef3 = prepareDefinition(BD3, Long.class);
        beanDef1.dependencies().put(beanDef2.name(), beanDef2.type());
        beanDef2.dependencies().put(beanDef3.name(), beanDef3.type());
        beanDef3.dependencies().put(beanDef1.name(), beanDef1.type());
        beanDefinitionList = List.of(beanDef1, beanDef2, beanDef3);

        assertExceptionAndMessage(expectedMessage, () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when beans have a circular dependency: A -> B; B -> C; C -> B")
    void circularDependencyClosed() {
        String expectedMessage = """
                Oops. Circular dependency occurs with bean: beanDef3 - java.lang.Long
                beanDef1 depends on: [beanDef2]
                beanDef2 depends on: [beanDef3]
                beanDef3 depends on: [beanDef2]""";
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        BeanDefinition beanDef2 = prepareDefinition(BD2, String.class);
        BeanDefinition beanDef3 = prepareDefinition(BD3, Long.class);
        beanDef1.dependencies().put(beanDef2.name(), beanDef2.type());
        beanDef2.dependencies().put(beanDef3.name(), beanDef3.type());
        beanDef3.dependencies().put(beanDef2.name(), beanDef2.type());
        beanDefinitionList = List.of(beanDef1, beanDef2, beanDef3);

        assertExceptionAndMessage(expectedMessage, () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when beans have a circular dependency: A -> B -> C -> D; D -> A and B")
    void circularDependencyExtended() {
        String expectedMessage = """
                Oops. Circular dependency occurs with bean: beanDef4 - java.lang.Byte
                beanDef1 depends on: [beanDef2]
                beanDef2 depends on: [beanDef3]
                beanDef3 depends on: [beanDef4]
                beanDef4 depends on: [beanDef2, beanDef1]""";
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        BeanDefinition beanDef2 = prepareDefinition(BD2, String.class);
        BeanDefinition beanDef3 = prepareDefinition(BD3, Long.class);
        BeanDefinition beanDef4 = prepareDefinition(BD4, Byte.class);
        beanDef1.dependencies().put(beanDef2.name(), beanDef2.type());
        beanDef2.dependencies().put(beanDef3.name(), beanDef3.type());
        beanDef3.dependencies().put(beanDef4.name(), beanDef4.type());
        beanDef4.dependencies().put(beanDef1.name(), beanDef1.type());
        beanDef4.dependencies().put(beanDef2.name(), beanDef2.type());
        beanDefinitionList = List.of(beanDef1, beanDef2, beanDef3, beanDef4);

        assertExceptionAndMessage(expectedMessage, () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when bean has a self-dependency: A -> A")
    void checkingSelfDependencyByBeanName() {
        String expectedMessage = """
                Oops. Circular dependency occurs with bean: beanDef1 - java.lang.Long
                beanDef1 depends on: [beanDef1]
                beanDef1 depends on: [beanDef1]""";
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        beanDef1.dependencies().put(beanDef1.name(), beanDef1.type());
        beanDefinitionList = List.of(beanDef1);

        assertExceptionAndMessage(expectedMessage, () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when bean has a self-dependency: A -> A")
    void checkingSelfDependency() {
        String expectedMessage = """
                Oops. Circular dependency occurs with bean: beanDef1 - java.lang.Long
                beanDef1 depends on: [beanDef1]
                beanDef1 depends on: [beanDef1]""";
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        beanDef1.dependencies().put(beanDef1.name(), beanDef1.type());
        beanDefinitionList = List.of(beanDef1);

        assertExceptionAndMessage(expectedMessage, () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails to find bean by custom name, but successfully finds it by type")
    void dependencyNotFoundByNameButFoundByType() {
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        BeanDefinition beanDef2 = prepareDefinition(BD2, String.class);
        BeanDefinition beanDef3 = prepareDefinition(BD3, Integer.class);
        BeanDefinition beanDef4 = prepareDefinition(BD4, Byte.class);

        beanDef1.dependencies().put(beanDef2.name(), beanDef2.type());
        beanDef2.dependencies().put(Byte.class.getName(), beanDef4.type());
        beanDef3.dependencies().put(beanDef1.name(), beanDef1.type());

        beanDefinitionList = List.of(beanDef1, beanDef2, beanDef3, beanDef4);
        assertDoesNotThrow(() -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when checking dependencies and bean was not found by custom name or by type")
    void checkingDependencyNotFoundDependencyByType() {
        String expectedMessageTemplate = "Unable to find bean with name `%s` and type %s in context";
        String dependencyName = BeanDefinition.class.getName();
        BeanDefinition beanDef1 = prepareDefinition(BD1, Long.class);
        BeanDefinition beanDef2 = prepareDefinition(BD2, String.class);
        BeanDefinition beanDef3 = prepareDefinition(BD3, Long.class);

        beanDef1.dependencies().put(beanDef2.name(), beanDef2.type());
        beanDef2.dependencies().put(beanDef3.name(), beanDef3.type());
        beanDef3.dependencies().put(dependencyName, BeanDefinition.class);
        beanDefinitionList = List.of(beanDef1, beanDef2, beanDef3);

        assertExceptionAndMessage(
                expectedMessageTemplate.formatted(dependencyName, dependencyName),
                () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when didn't find dependency by default name - class.getName(), but found by type more than 1")
    void moreThanOneBeanByType() {
        String expectedMessage = "Found more than 1 bean with type java.lang.Integer in context";
        String dependencyName = Integer.class.getName();
        BeanDefinition beanDef1 = prepareDefinition("beanDef1", Integer.class);
        BeanDefinition beanDef2 = prepareDefinition(BD2, Integer.class);
        beanDef1.dependencies().put(dependencyName, Integer.class);
        beanDefinitionList = List.of(beanDef1, beanDef2);

        assertExceptionAndMessage(expectedMessage, () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails when didn't find dependency by custom name and type")
    void dependenciesNotFoundByCustomName() {
        String expectedMessageTemplate = "Unable to find bean with name `%s` and type %s in context";
        BeanDefinition beanDef1 = prepareDefinition("beanDef1", Integer.class);
        beanDef1.dependencies().put("anotherName", BeanDefinition.class);
        beanDefinitionList = List.of(beanDef1);

        assertExceptionAndMessage(expectedMessageTemplate.formatted("anotherName", BeanDefinition.class.getName()),
                () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Looks for primary bean when more than one bean found with the same type")
    void primaryBeanIsFound() {
        BeanDefinition bean = prepareDefinition("bean", Object.class);
        Class<Integer> type = Integer.class;
        BeanDefinition primaryBean = prepareDefinition("primaryBean", type);
        BeanDefinition nonPrimaryBean = prepareDefinition("nonPrimaryBean", type);
        bean.dependencies().put(type.getName(), type);
        when(primaryBean.isPrimary()).thenReturn(true);

        beanDefinitionList = List.of(bean, primaryBean, nonPrimaryBean);

        assertDoesNotThrow(() -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    @Test
    @DisplayName("Fails on more than one primary bean of the same type")
    void moreThanOnePrimaryBean() {
        BeanDefinition bean = prepareDefinition("bean", Object.class);
        Class<Integer> type = Integer.class;
        BeanDefinition primaryBean1 = prepareDefinition("primaryBean", type);
        BeanDefinition primaryBean2 = prepareDefinition("nonPrimaryBean", type);
        bean.dependencies().put(type.getName(), type);
        when(primaryBean1.isPrimary()).thenReturn(true);
        when(primaryBean2.isPrimary()).thenReturn(true);

        beanDefinitionList = List.of(bean, primaryBean1, primaryBean2);

        assertExceptionAndMessage("Found more than 1 primary bean with type java.lang.Integer in context",
                () -> beanDefinitionValidator.validate(beanDefinitionList));
    }

    private void assertExceptionAndMessage(String expectedMessage, Executable executable) {
        BeanValidationException ex = assertThrows(BeanValidationException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    private static BeanDefinition prepareDefinition(String beanDefinitionName, Class<?> type,
                                                    BeanDefinition... beanDefinitions) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(type).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(beanDefinitionName);

        Map<String, Class<?>> dependencies = new HashMap<>();
        for (var dependency : beanDefinitions) {
            dependencies.put(dependency.name(), dependency.type());
        }
        when(beanDefinition.dependencies()).thenReturn(dependencies);

        return beanDefinition;
    }
}
