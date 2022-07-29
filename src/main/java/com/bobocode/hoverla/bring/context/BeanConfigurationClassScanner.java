package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A bean definition {@link BeanScanner scanner} that detects bean candidates defined as methods marked
 * with {@link Bean @Bean} annotation in class annotated with {@link Configuration @Configuration}
 *
 * @see Bean @Bean
 * @see Configuration @Configuration
 * @see BeanScanner
 */
@Slf4j
public class BeanConfigurationClassScanner implements BeanScanner {

    private final String[] packagesToScan;
    private final BeanConfigurationClassValidator validator;
    private final BeanDefinitionMapper mapper;

    public BeanConfigurationClassScanner(BeanConfigurationClassValidator validator,
                                         BeanDefinitionMapper mapper,
                                         String... packagesToScan) {
        this.packagesToScan = packagesToScan;
        this.validator = validator;
        this.mapper = mapper;
    }

    /**
     * Performs scan of classes annotated with {@link Configuration @Configuration}
     * from given {@link BeanConfigurationClassScanner#packagesToScan packages}.
     *
     * <p>Scanned {@link Class} objects are then transferred to {@link BeanConfigurationClassValidator validator} for further validation process.</p>
     * <p>{@link BeanDefinitionMapper} is used to map scanned {@link Class} objects to {@link BeanDefinition}.</p>
     *
     * @return {@link List} of {@link BeanDefinition} objects.
     * @see BeanDefinition
     * @see BeanConfigurationClassValidator
     * @see BeanDefinitionMapper
     */
    @Override
    public List<BeanDefinition> scan() {
        log.info("{} packages for scan received. Starting scan of classes annotated with '@Configuration'", packagesToScan.length);
        Reflections reflections = new Reflections((Object[]) packagesToScan);
        Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(Configuration.class);

        if (configClasses.isEmpty()) {
            log.warn("No classes annotated with `@Configuration` found during scan in packages {}", Arrays.toString(packagesToScan));
            return Collections.emptyList();
        }
        log.debug("Successfully scanned {} `@Configuration` classes", configClasses.size());

        return configClasses.stream()
                .map(this::scanBeanConfigMethods)
                .flatMap(List::stream)
                .toList();
    }

    private List<BeanDefinition> scanBeanConfigMethods(Class<?> configClass) {
        validator.validate(configClass);
        Object configClassInstance = createConfigClassInstance(configClass);

        return resolveBeanMethods(configClass).stream()
                .map(method -> mapper.mapToBeanDefinition(configClassInstance, method))
                .toList();
    }

    private List<Method> resolveBeanMethods(Class<?> configClass) {
        return Arrays.stream(configClass.getMethods())
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .toList();
    }

    /**
     * Creates instance of config class to share it across all bean definitions of the target config class.
     * No-arg constructor presence is checked during config class {@link BeanConfigurationClassValidator validation}.
     */
    @SneakyThrows
    private Object createConfigClassInstance(Class<?> configClass) {
        return configClass.getConstructor().newInstance();
    }
}
