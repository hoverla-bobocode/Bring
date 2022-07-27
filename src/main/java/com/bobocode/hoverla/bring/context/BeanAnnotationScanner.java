package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A bean definition scanner that detects bean candidates on the classpath.
 *
 * <p>Candidate classes are detected through annotation {@link Bean @Bean}.</p>
 *
 * @see Bean @Bean
 * @see BeanScanner
 */
@Slf4j
public class BeanAnnotationScanner implements BeanScanner {

    private final BeanAnnotationClassValidator validator;

    private final BeanDefinitionMapper mapper;

    private final String[] packagesToScan;

    public BeanAnnotationScanner(BeanAnnotationClassValidator validator, BeanDefinitionMapper mapper,
                                 String... packagesToScan) {
        this.validator = validator;
        this.mapper = mapper;
        this.packagesToScan = packagesToScan;
    }

    /**
     * Performs scan of classes annotated with {@link Bean @Bean} from given {@link BeanAnnotationScanner#packagesToScan}.
     *
     * <p>Scanned {@link Class} objects are then transferred to {@link BeanAnnotationClassValidator} for further validation process.</p>
     * <p>{@link BeanDefinitionMapper} is used to map scanned {@link Class} objects to {@link BeanDefinition}.</p>
     *
     * @return {@link List} of {@link BeanDefinition} objects.
     *
     * @see BeanDefinition
     * @see BeanAnnotationClassValidator
     * @see BeanDefinitionMapper
     */
    @Override
    public List<BeanDefinition> scan() {
        log.info("{} packages for scan received. Starting scan of classes annotated with '@Bean'", this.packagesToScan.length);
        Reflections reflectionScanner = new Reflections((Object[]) this.packagesToScan);
        Set<Class<?>> beanClasses = reflectionScanner.getTypesAnnotatedWith(Bean.class);

        if (beanClasses.isEmpty()) {
            log.warn("No classes annotated with `@Bean` found during scan in packages {}", Arrays.toString(this.packagesToScan));
            return Collections.emptyList();
        }
        log.debug("Successfully scanned {} `@Bean` classes", beanClasses.size());

        this.validator.validateBeanClasses(beanClasses);

        return beanClasses.stream()
                .map(mapper::mapToBeanDefinition)
                .toList();
    }
}
