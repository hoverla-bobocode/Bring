package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanClassValidationException;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * Validates classes annotated with {@link Bean @Bean} that were scanned by {@link BeanAnnotationScanner}.
 *
 * <p>Receives a {@link Set} of {@link Class} objects and performs type, instance fields and constructors checks.</p>
 * Validation ensures:
 * <ol>
 *     <li>Valid type of a {@link Class} object.
 *     <br>See {@link BeanAnnotationClassValidator#validateClass(Class)}</li>
 *     <li>Valid declared constructor - one constructor declaration allowed, plain constructors are not
 *     mixed with constructors annotated with {@link Inject @Inject}.
 *     <br>See {@link BeanAnnotationClassValidator#validateConstructors(Class, List)}</li>
 *     <li>Valid declared constructor parameters - there should be no parameters with duplicated {@link Qualifier @Qualifier} value
 *     or parameters of the same type without {@link Qualifier @Qualifier}.
 *     <br>See {@link BeanAnnotationClassValidator#validateConstructorParameters(List, List)}</li>
 *     <li>Valid declared instance fields - fields annotated with {@link Inject @Inject}
 *     are neither {@code static} nor {@code final}.
 *     There should be no fields of the same type without {@link Qualifier @Qualifier} annotation.
 *     There should be no fields with the same {@link Qualifier @Qualifier} value.</li>
 *     <br>See {@link BeanAnnotationClassValidator#validateFields(Class, List)}
 * </ol>
 *
 * @see Bean
 * @see Inject
 * @see BeanAnnotationScanner
 */
@Slf4j
public class BeanAnnotationClassValidator {

    private static final String VIOLATION_MESSAGES_DELIMITER = "\n- ";

    private static final Map<String, Predicate<Class<?>>> UNSUPPORTED_TYPE_PREDICATE_MAP = Map.of(
            "Interface", Class::isInterface,
            "Enum", Class::isEnum,
            "Record", Class::isRecord,
            "Abstract class", type -> Modifier.isAbstract(type.getModifiers()),
            "Inner type ", type -> Objects.nonNull(type.getEnclosingClass())
    );

    /**
     * Performs validation of scanned {@link Class} objects.
     *
     * @param beanClasses {@link Set} of scanned {@link Class} objects, returned by {@link BeanAnnotationScanner}.
     * @throws BeanClassValidationException when any validation constraint is violated.
     * @see BeanAnnotationScanner
     */
    public void validateBeanClasses(Set<Class<?>> beanClasses) {
        log.info("Started validation of {} scanned `@Bean` classes", beanClasses.size());
        for (Class<?> beanClass : beanClasses) {
            List<String> validationViolations = new ArrayList<>();
            String beanClassName = beanClass.getName();

            log.trace("Validating class - {}", beanClassName);
            validateClass(beanClass);
            validateConstructors(beanClass, validationViolations);
            validateFields(beanClass, validationViolations);

            if (!validationViolations.isEmpty()) {
                log.error("Validation failed for class - {}", beanClassName);

                int violationsAmount = validationViolations.size();
                String formattedViolations = String.join(VIOLATION_MESSAGES_DELIMITER, validationViolations);

                String exceptionMessage = format("Found %d validation errors for class %s: %n- %s",
                        violationsAmount, beanClassName, formattedViolations);

                throw new BeanClassValidationException(exceptionMessage);
            }
        }
    }

    private void validateClass(Class<?> beanClass) {
        UNSUPPORTED_TYPE_PREDICATE_MAP.entrySet()
                .stream()
                .filter(predicateEntry -> predicateEntry.getValue().test(beanClass))
                .findFirst()
                .ifPresent(predicateEntry -> {
                    log.error("Unsupported bean type found for class - {}", beanClass.getSimpleName());
                    throw new BeanClassValidationException(
                            format("Class marked as @Bean is of unsupported type - %s", predicateEntry.getKey()));
                });
    }

    private void validateConstructors(Class<?> beanClass, List<String> validationViolations) {
        List<Constructor<?>> beanConstructors = asList(beanClass.getConstructors());
        log.trace("Overall number of constructors found - {}", beanConstructors.size());
        if (beanConstructors.isEmpty()) {
            validationViolations.add("Class has no public constructors");
            return;
        }

        List<Constructor<?>> injectionConstructors = getElementsAnnotatedWith(beanConstructors, Inject.class);
        if (!injectionConstructors.isEmpty()) {
            validateInjectionConstructors(injectionConstructors, validationViolations);
        } else {
            log.trace("No @Inject constructor found. Trying to find plain one");
            validatePlainConstructors(beanConstructors, validationViolations);
        }
    }

    private void validateInjectionConstructors(List<Constructor<?>> injectionConstructors,
                                               List<String> validationViolations) {
        int constructorAmount = injectionConstructors.size();
        log.trace("Found {} @Inject constructors", constructorAmount);
        if (constructorAmount > 1) {
            validationViolations.add(
                    format("Class has %d constructors marked with @Inject. Unable to pick up one", constructorAmount));
            return;
        }

        Constructor<?> beanConstructor = injectionConstructors.get(0);
        log.trace("Single @Inject constructor found - {}", beanConstructor);

        if (beanConstructor.getParameterCount() == 0) {
            validationViolations.add("@Inject constructor has no parameters");
        } else {
            List<Parameter> constructorParameters = asList(beanConstructor.getParameters());
            validateConstructorParameters(constructorParameters, validationViolations);
        }
    }

    private void validatePlainConstructors(List<Constructor<?>> beanConstructors, List<String> validationViolations) {
        if (beanConstructors.size() > 1) {
            validationViolations.add(format("Class has %d plain constructors. Unable to pick up one", beanConstructors.size()));
            return;
        }
        Constructor<?> beanConstructor = beanConstructors.get(0);
        log.trace("Single plain constructor found - {}", beanConstructor);

        int parameterCount = beanConstructor.getParameterCount();
        if (parameterCount > 0) {
            List<Parameter> constructorParameters = asList(beanConstructor.getParameters());
            log.trace("Constructor has {} parameters - will be used for injection", parameterCount);
            validateConstructorParameters(constructorParameters, validationViolations);
        }
    }

    private void validateConstructorParameters(List<Parameter> constructorParameters, List<String> validationViolations) {
        log.trace("Validating constructor parameters");
        List<Parameter> qualifiedParameters = getElementsAnnotatedWith(constructorParameters, Qualifier.class);
        List<Parameter> nonQualifiedParameters = ListUtils.removeAll(constructorParameters, qualifiedParameters);

        validateQualifiedParameters(qualifiedParameters, validationViolations);
        validateNonQualifiedParameters(nonQualifiedParameters, validationViolations);
    }

    private void validateQualifiedParameters(List<Parameter> qualifiedParameters, List<String> validationViolations) {
        Map<String, List<String>> parametersWithSameQualifier = qualifiedParameters.stream()
                .collect(groupingBy(p -> p.getAnnotation(Qualifier.class).value(), mapping(Parameter::getName, toList())));

        Maps.filterValues(parametersWithSameQualifier, names -> names.size() > 1)
                .forEach((qualifier, paramNames) -> validationViolations.add(
                        format("Found several constructor parameters with same @Qualifier value `%s` - %s",
                                qualifier, paramNames))
                );
    }

    private void validateNonQualifiedParameters(List<Parameter> nonQualifiedParameters,
                                                List<String> validationViolations) {
        Map<Class<?>, List<String>> parametersWithSameType = nonQualifiedParameters.stream()
                .collect(groupingBy(Parameter::getType, mapping(Parameter::getName, toList())));

        Maps.filterValues(parametersWithSameType, paramNames -> paramNames.size() > 1)
                .forEach((type, fieldNames) -> validationViolations.add(
                        format("Found several constructor parameters of type %s without @Qualifier - %s",
                                type.getName(), fieldNames))
                );
    }

    private void validateFields(Class<?> beanClass, List<String> validationViolations) {
        List<Field> classFields = asList(beanClass.getDeclaredFields());

        List<Field> fieldsForInjection = getElementsAnnotatedWith(classFields, Inject.class);
        if (fieldsForInjection.isEmpty()) {
            log.trace("No fields marked with @Inject found - aborting field validation");
            return;
        }

        log.trace("Found {} fields marked with @Inject", fieldsForInjection.size());

        validateFieldsModifiers(fieldsForInjection, validationViolations);

        List<Field> qualifiedFields = getElementsAnnotatedWith(fieldsForInjection, Qualifier.class);
        List<Field> nonQualifiedFields = ListUtils.removeAll(fieldsForInjection, qualifiedFields);

        validateQualifiedFields(qualifiedFields, validationViolations);
        validateNonQualifiedFields(nonQualifiedFields, validationViolations);
    }

    private void validateQualifiedFields(List<Field> qualifiedFields, List<String> validationViolations) {
        Map<String, List<String>> fieldsWithSameQualifier = qualifiedFields.stream()
                .collect(groupingBy(f -> f.getAnnotation(Qualifier.class).value(), mapping(Field::getName, toList())));

        Maps.filterValues(fieldsWithSameQualifier, fieldNames -> fieldNames.size() > 1)
                .forEach((qualifier, fieldNames) -> validationViolations.add(
                        format("Found several fields with same @Qualifier value `%s` - %s", qualifier, fieldNames))
                );
    }

    private void validateNonQualifiedFields(List<Field> nonQualifiedFields, List<String> validationViolations) {
        Map<Class<?>, List<String>> fieldsWithSameType = nonQualifiedFields.stream()
                .collect(groupingBy(Field::getType, mapping(Field::getName, toList())));

        Maps.filterValues(fieldsWithSameType, fieldNames -> fieldNames.size() > 1)
                .forEach((type, fieldNames) -> validationViolations.add(
                        format("Found several fields of type %s without @Qualifier - %s", type.getName(), fieldNames))
                );
    }

    private void validateFieldsModifiers(List<Field> fieldsForInjection, List<String> validationViolations) {
        for (Field field : fieldsForInjection) {
            String fieldName = field.getName();
            int fieldModifiers = field.getModifiers();
            if (Modifier.isFinal(fieldModifiers) || Modifier.isStatic(fieldModifiers)) {
                validationViolations.add(format("Field marked with @Inject cannot be static/final - %s", fieldName));
            }
        }
    }

    private <E extends AnnotatedElement> List<E> getElementsAnnotatedWith(List<E> elements,
                                                                          Class<? extends Annotation> annotationClass) {
        return elements.stream()
                .filter(member -> member.isAnnotationPresent(annotationClass))
                .toList();
    }
}
