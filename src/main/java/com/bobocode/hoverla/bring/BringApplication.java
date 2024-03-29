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
import com.bobocode.hoverla.bring.context.BeanDependencyNameResolver;
import com.bobocode.hoverla.bring.context.BeanInitializer;
import com.bobocode.hoverla.bring.context.BeanScanner;
import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
@UtilityClass
public class BringApplication {
    private final CharSequence[] ILLEGAL_SYMBOLS = {"^","!","@","#","$","%","^","&","*","(",")","?","~","+","-","<",">","/",","};

    /**
     * Initializes and returns {@link ApplicationContext}
     * @param packagesToScan packages to scan
     */
    public ApplicationContext loadContext(String... packagesToScan) {
        return createContext(packagesToScan);
    }

    /**
     * Initializes input parameters (list of {@link BeanScanner}, {@link BeanDefinitionValidator}, {@link BeanInitializer})
     * for ApplicationContext creation and return instance of context
     *
     * @param packagesToScan packages for scanning to define bean configs
     * @return instance of {@link ApplicationContextImpl}
     */
    private ApplicationContext createContext(String... packagesToScan) {
        validatePackagesToScan(packagesToScan);

        List<BeanScanner> scanners = createBeanScanners(packagesToScan);
        var beanDefinitionValidator = new BeanDefinitionValidator();
        var dependencyNameResolver = new BeanDependencyNameResolver();
        var initializer = new BeanInitializer(dependencyNameResolver);

        return new ApplicationContextImpl(scanners, beanDefinitionValidator, initializer);
    }

    private List<BeanScanner> createBeanScanners(String[] packagesToScan) {
        var beanDefinitionMapper = new BeanDefinitionMapper();

        var beanAnnotationClassValidator = new BeanAnnotationClassValidator();
        var beanAnnotationScanner = new BeanAnnotationScanner(beanAnnotationClassValidator, beanDefinitionMapper, packagesToScan);

        var beanConfigurationClassValidator = new BeanConfigurationClassValidator();
        var beanConfigurationClassScanner = new BeanConfigurationClassScanner(beanConfigurationClassValidator, beanDefinitionMapper, packagesToScan);

        return List.of(beanAnnotationScanner, beanConfigurationClassScanner);
    }

    private static void validatePackagesToScan(String... packagesToScan) {
        if (ArrayUtils.isEmpty(packagesToScan)) {
            throw new IllegalArgumentException("Argument [packagesToScan] must contain at least one not null and not empty element");
        }
        if (Arrays.stream(packagesToScan)
                .anyMatch(Strings::isNullOrEmpty)) {
            throw new IllegalArgumentException("Argument [packagesToScan] must not contain null or empty element");
        }
        if (Arrays.stream(packagesToScan)
                .anyMatch(p -> StringUtils.containsAny(p, ILLEGAL_SYMBOLS))) {
            throw new IllegalArgumentException("Package name must contain only letters, numbers and symbol [.]");
        }
    }

    /**
     * Creates instance of ApplicationContextBuilder class
     * @return ApplicationContextBuilder instance
     */
    public ApplicationContextBuilder getContextBuilder() {
        return new ApplicationContextBuilder();
    }

    /**
     *
     */
    public class ApplicationContextBuilder {

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
