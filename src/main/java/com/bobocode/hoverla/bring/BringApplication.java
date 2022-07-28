package com.bobocode.hoverla.bring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import com.bobocode.hoverla.bring.context.ApplicationContextImpl;
import com.bobocode.hoverla.bring.context.BeanDefinitionValidator;
import com.bobocode.hoverla.bring.context.BeanInitializer;
import com.bobocode.hoverla.bring.context.BeanScanner;
import com.google.common.base.Strings;
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
        return getContext(packagesToScan);
    }

    /**
     * Initializes input parameters (list of {@link BeanScanner}, {@link BeanDefinitionValidator}, {@link BeanInitializer})
     * for ApplicationContext creation and return instance of context
     *
     * @param packagesToScan packages for scanning to define bean configs
     * @return instance of {@link ApplicationContextImpl}
     */
    private static ApplicationContext getContext(String... packagesToScan)  {
        // TODO: use this variable for scanners
        String[] validatedPackages = validatePackagesToScan(packagesToScan);
        // TODO: add initialization of scanners;
        List<BeanScanner> scanners = List.of();

        return new ApplicationContextImpl(scanners, new BeanDefinitionValidator(), new BeanInitializer());
    }

    private static String[] validatePackagesToScan(String... packagesToScan) {
        String message = "Argument [packagesToScan] must contain at least one not null and not empty element";
        if (packagesToScan == null || packagesToScan.length == 0) {
            throw new IllegalArgumentException(message);
        }

        String[] filteredPackages = Arrays.stream(packagesToScan)
                .filter(p -> !Strings.isNullOrEmpty(p))
                .toArray(String[]::new);
        if (filteredPackages.length == 0) {
            throw new IllegalArgumentException(message);
        } else {
            return filteredPackages;
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
    static class ApplicationContextBuilder {
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

        @SuppressWarnings(value = "see https://rules.sonarsource.com/java/RSPEC-4792")
        public ApplicationContext build() {
            Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            logger.setLevel(logLevel != null ? logLevel : Level.INFO);

            return getContext(packagesToScan);
        }

    }

}
