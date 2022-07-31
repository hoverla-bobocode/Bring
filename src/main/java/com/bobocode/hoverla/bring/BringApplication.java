package com.bobocode.hoverla.bring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import com.bobocode.hoverla.bring.context.ApplicationContextImpl;
import com.bobocode.hoverla.bring.context.BeanAnnotationClassValidator;
import com.bobocode.hoverla.bring.context.BeanAnnotationScanner;
import com.bobocode.hoverla.bring.context.BeanConfigurationClassScanner;
import com.bobocode.hoverla.bring.context.BeanConfigurationClassValidator;
import com.bobocode.hoverla.bring.context.BeanDefinitionMapper;
import com.bobocode.hoverla.bring.context.BeanDefinitionValidator;
import com.bobocode.hoverla.bring.context.BeanInitializer;
import com.bobocode.hoverla.bring.context.BeanScanner;
import com.bobocode.hoverla.bring.context.LateBeanCreator;
import com.google.common.base.Strings;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * Bring starting point. Initializes and returns {@link ApplicationContext} encapsulating
 * all necessary logic for its creation.
 *
 * Usage:
 * {@code
 *     ApplicationContext context = BringApplication.loadContext("com.bobocode.hoverla.bring");
 *     String bean = context.getBean("bean_name", String.class);
 * }
 * </pre>
 * <p>
 * BringApplication provides possibility to define log level. Use {@link ApplicationContextBuilder} for this purpose.
 * <p>
 * Usage:
 * {@code
 *      ApplicationContext context = BringApplication.getContextBuilder()
 *              .logLevel(Level.DEBUG)
 *              .build();
 *      String bean = context.getBean("BeanName");
 * }
 *
 * It is necessary to provide packages to be scanned to define bean definition configs.
 */
public class BringApplication {
    private BringApplication() {
    }

    /**
     * Initializes and returns {@link ApplicationContext}
     * @param packagesToScan packages to scan
     */
    public static ApplicationContext loadContext(String... packagesToScan) {
        return createContext(packagesToScan);
    }

    /**
     * Initializes input parameters (list of {@link BeanScanner}, {@link BeanDefinitionValidator}, {@link BeanInitializer})
     * for ApplicationContext creation and return instance of context
     *
     * @param packagesToScan packages for scanning to define bean configs
     * @return instance of {@link ApplicationContextImpl}
     */
    private static ApplicationContext createContext(String... packagesToScan) {
        validatePackagesToScan(packagesToScan);

        BeanDefinitionMapper beanDefinitionMapper = new BeanDefinitionMapper();
        BeanAnnotationClassValidator beanAnnotationClassValidator = new BeanAnnotationClassValidator();
        BeanAnnotationScanner beanAnnotationScanner = new BeanAnnotationScanner(beanAnnotationClassValidator, beanDefinitionMapper, packagesToScan);
        BeanConfigurationClassValidator beanConfigurationClassValidator = new BeanConfigurationClassValidator();
        BeanConfigurationClassScanner beanConfigurationClassScanner = new BeanConfigurationClassScanner(beanConfigurationClassValidator, beanDefinitionMapper, packagesToScan);

        List<BeanScanner> scanners = List.of(beanAnnotationScanner, beanConfigurationClassScanner);
        BeanDefinitionValidator beanDefinitionValidator = new BeanDefinitionValidator();
        BeanInitializer initializer = new BeanInitializer();
        LateBeanCreator lateBeanCreator = new LateBeanCreator(beanAnnotationClassValidator, beanDefinitionMapper);
        return new ApplicationContextImpl(scanners, beanDefinitionValidator, lateBeanCreator, initializer);
    }

    private static void validatePackagesToScan(String... packagesToScan) {
        String message = "Argument [packagesToScan] must contain at least one not null and not empty element";
        if (ArrayUtils.isEmpty(packagesToScan)) {
            throw new IllegalArgumentException(message);
        }

        String[] filteredPackages = Arrays.stream(packagesToScan)
                .filter(p -> !Strings.isNullOrEmpty(p))
                .toArray(String[]::new);
        if (filteredPackages.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Creates instance of ApplicationContextBuilder class
     * @return ApplicationContextBuilder instance
     */
    public static ApplicationContextBuilder getContextBuilder() {
        return new ApplicationContextBuilder();
    }

    /**
     *
     */
    public static class ApplicationContextBuilder {
        private Level logLevel;
        private String[] packagesToScan;

        public ApplicationContextBuilder logLevel(Level logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public ApplicationContextBuilder packagesToScan(String... packagesToScan) {
            this.packagesToScan = packagesToScan;
            return this;
        }

        public ApplicationContext build() {
            Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            logger.setLevel(logLevel != null ? logLevel : Level.INFO);
            return createContext(packagesToScan);
        }
    }

}
