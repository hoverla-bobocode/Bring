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
 *   <br>See {@link BeanConfigurationClassValidator#checkClassModifiers(Class, List)} and {@link BeanConfigurationClassValidator#checkConfigIsPlainClass(Class, List)}</li>
 *   <li>Valid bean methods - they should be public and non-static.
 *   <br>See {@link BeanConfigurationClassValidator#validateBeanMethod(Method, List)}</li>
 *   <li>Valid constructor - bean config class should have a public non-args constructor.
 *   <br>See {@link BeanConfigurationClassValidator#checkNoArgsConstructorExists(Class, List)}
 * </ol>
 *
 * @see Bean
 * @see Configuration
 * @see BeanScanner
 */
@Slf4j
public class BeanConfigurationClassValidator {

    private static final String VIOLATION_MSG_DELIMITER = "\n-";

    /**
     * Receives config class and checks violations of bean config class and its bean methods
     *
     * @param configClass target bean configuration class
     */
    public void validate(Class<?> configClass) {
        List<String> validationMessages = new ArrayList<>();
        validateConfigClass(configClass, validationMessages);
        validateMethods(configClass.getDeclaredMethods(), validationMessages);

        if (validationMessages.isEmpty()) {
            return;
        }
        String violations = String.join(VIOLATION_MSG_DELIMITER, validationMessages);
        String message = "%s class violates bean configuration class rules: %s%s"
                .formatted(configClass.getName(), violations, VIOLATION_MSG_DELIMITER);
        throw new BeanConfigValidationException(message);
    }

    private void validateMethods(Method[] methods, List<String> validationMessages) {
        Arrays.stream(methods)
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .forEach(m -> validateBeanMethod(m, validationMessages));
    }

    private void validateConfigClass(Class<?> configClass, List<String> validationMessages) {
        checkClassModifiers(configClass, validationMessages);
        checkConfigIsPlainClass(configClass, validationMessages);
        checkNoArgsConstructorExists(configClass, validationMessages);
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

    private void validateBeanMethod(Method method, List<String> validationMessages) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            validationMessages.add("%s method must not be static".formatted(method.getName()));
        }
        if (!Modifier.isPublic(modifiers)) {
            validationMessages.add("%s method must be public".formatted(method.getName()));
        }
    }
}
