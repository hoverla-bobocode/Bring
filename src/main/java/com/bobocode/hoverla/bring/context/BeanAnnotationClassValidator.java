package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.exception.BeanClassValidationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.lang.reflect.Array.getLength;

/**
 * Validates classes annotated with {@link Bean @Bean} that were scanned by {@link BeanAnnotationScanner}.
 *
 * <p>Receives a {@link Set} of {@link Class} objects and performs type, instance fields and constructors checks.</p>
 *
 * <p>Validation ensures:
 * <br>1. Valid type of a {@link Class} object - see {@code UNSUPPORTED_TYPE_PREDICATE_MAP}.</br>
 * <br>2. Valid declared constructor - one constructor declaration allowed, plain constructors
 * are not mixed with constructors annotated with {@link Inject @Inject}.</br>
 * <br>3. Valid declared instance fields - fields annotated with {@link Inject @Inject} are neither {@code static} nor {@code final}.</br>
 * </p>
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
     *
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
                String exceptionMessage = format("Found %d validation errors for class %s: %n- %s",
                        violationsAmount, beanClassName, constructExceptionMessage(validationViolations));

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

    private void validateFields(Class<?> beanClass, List<String> validationViolations) {
        Field[] classFields = beanClass.getDeclaredFields();
        List<Field> fieldsForInjection = getMembersMarkedWithInject(classFields);

        log.trace("Found {} fields marked with @Inject", fieldsForInjection.size());
        for (Field field : fieldsForInjection) {
            String fieldName = field.getName();
            int fieldModifiers = field.getModifiers();
            if (Modifier.isFinal(fieldModifiers) || Modifier.isStatic(fieldModifiers)) {
                validationViolations.add(format("Field marked with @Inject cannot be static/final - %s", fieldName));
            }
        }
    }

    private void validateConstructors(Class<?> beanClass, List<String> validationViolations) {
        Constructor<?>[] beanConstructors = beanClass.getConstructors();
        int constructorAmount = getLength(beanConstructors);
        log.trace("Overall number of constructors found - {}", constructorAmount);
        if (constructorAmount == 0) {
            validationViolations.add("Class has no public constructors");
            return;
        }

        List<Constructor<?>> injectionConstructors = getMembersMarkedWithInject(beanConstructors);
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
        }
    }

    private void validatePlainConstructors(Constructor<?>[] beanConstructors, List<String> validationViolations) {
        int constructorAmount = getLength(beanConstructors);
        log.trace("Found {} plain constructors", constructorAmount);
        if (constructorAmount > 1) {
            validationViolations.add(format("Class has %d plain constructors. Unable to pick up one", constructorAmount));
            return;
        }

        Constructor<?> beanConstructor = beanConstructors[0];
        log.trace("Single plain constructor found - {}", beanConstructor);

        int parameterCount = beanConstructor.getParameterCount();
        if (parameterCount > 0) {
            log.trace("Constructor has {} parameters - will be used for injection", parameterCount);
        }
    }

    private <M extends AnnotatedElement> List<M> getMembersMarkedWithInject(M[] classMembers) {
        return Arrays.stream(classMembers)
                .filter(member -> member.isAnnotationPresent(Inject.class))
                .toList();
    }

    private String constructExceptionMessage(List<String> validationViolations) {
        return String.join(VIOLATION_MESSAGES_DELIMITER, validationViolations);
    }
}
