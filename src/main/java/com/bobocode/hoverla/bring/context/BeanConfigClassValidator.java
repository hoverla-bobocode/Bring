package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.exception.BeanConfigValidationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates classes annotated with {@link Configuration @Configuration} and
 * their methods marked with {@link Bean @Bean} that were scanned by {@link BeanScanner}.
 *
 * <p>Validation ensures:
 * <ol>
 *   <li>Valid type of a {@link Class} config object - it should be plain public class.
 *   <br>See {@link BeanConfigClassValidator#checkClassModifiers(Class, List)} and {@link BeanConfigClassValidator#checkConfigIsPlainClass(Class, List)}</li>
 *   <li>Valid bean methods - they should be public and non-static.
 *   <br>See {@link BeanConfigClassValidator#checkMethodModifiers(Method, List)}</li>
 *   <li>Valid constructor - bean config class should have a public non-args constructor.
 *   <br>See {@link BeanConfigClassValidator#checkNoArgsConstructorExists(Class, List)}
 * </ol>
 *
 * @see Bean
 * @see Configuration
 * @see BeanScanner
 */
@Slf4j
class BeanConfigClassValidator {
    private static final String VIOLATION_MSG_DELIMITER = "\n-";

    /**
     * Receives config class and checks violations of bean config class and its bean methods
     *
     * @param configClass target bean configuration class
     */
    void validate(Class<?> configClass) {
        List<String> validationMessages = validateConfigClass(configClass);
        List<String> methodsValidationMessages = validateMethods(configClass.getDeclaredMethods());

        validationMessages.addAll(methodsValidationMessages);
        if (validationMessages.isEmpty()) {
            return;
        }
        String violations = String.join(VIOLATION_MSG_DELIMITER, validationMessages);
        String message = "%s class violates bean configuration class rules: %s%s"
                .formatted(configClass.getName(), violations, VIOLATION_MSG_DELIMITER);
        throw new BeanConfigValidationException(message);
    }

    private List<String> validateMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .map(this::validateBeanMethod)
                .flatMap(List::stream)
                .toList();
    }

    private List<String> validateConfigClass(Class<?> configClass) {
        List<String> messages = new ArrayList<>();
        checkClassModifiers(configClass, messages);
        checkConfigIsPlainClass(configClass, messages);
        checkNoArgsConstructorExists(configClass, messages);
        return messages;
    }

    private void checkClassModifiers(Class<?> configClass, List<String> messages) {
        int modifiers = configClass.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            messages.add("Configuration class must be public");
        }
        if (Modifier.isAbstract(modifiers)) {
            messages.add("Configuration class must not be an abstract class");
        }
    }

    private void checkConfigIsPlainClass(Class<?> configClass, List<String> messages) {
        if (configClass.getEnclosingClass() != null) {
            messages.add("Configuration class must not have an enclosing class");
        }
        if (configClass.isRecord()) {
            messages.add("Configuration class must not be a record");
        }
        if (configClass.isEnum()) {
            messages.add("Configuration class must not be an enum");
        }
        if (configClass.isInterface()) {
            messages.add("Configuration class must not be an interface");
        }
    }

    private void checkNoArgsConstructorExists(Class<?> configClass, List<String> messages) {
        Constructor<?>[] publicConstructors = configClass.getConstructors();
        boolean noDefaultConstructorFound = Arrays.stream(publicConstructors)
                .noneMatch(c -> c.getParameterCount() == 0);

        if (noDefaultConstructorFound) {
            messages.add("Configuration class must have one public no-arguments constructor");
        }
    }

    private List<String> validateBeanMethod(Method method) {
        List<String> messages = new ArrayList<>();
        checkMethodModifiers(method, messages);
        return messages;
    }

    private void checkMethodModifiers(Method method, List<String> messages) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            messages.add("%s method must not be static".formatted(method.getName()));
        }
        if (!Modifier.isPublic(modifiers)) {
            messages.add("%s method must be public".formatted(method.getName()));
        }
    }
}
