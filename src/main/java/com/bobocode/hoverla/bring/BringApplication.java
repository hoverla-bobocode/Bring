package com.bobocode.hoverla.bring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.bobocode.hoverla.bring.context.ApplicationContext;
import com.bobocode.hoverla.bring.context.ApplicationContextImpl;
import com.bobocode.hoverla.bring.context.BeanDefinitionValidator;
import com.bobocode.hoverla.bring.context.BeanInitializer;
import com.bobocode.hoverla.bring.context.BeanScanner;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Bring starting point. Initializes and returns {@link ApplicationContext} encapsulating
 * all necessary logic for its creation.
 *
 * Usage:
 * {@code
 *     ApplicationContext = BringApplication.loadContext("com.bobocode.hoverla.bring");
 *     String bean = ApplicationContext.getBean("bean_name", String.class);
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
 * It is necessary to provide packages will be scan to define bean definition configs. User can pass either
 * package names or user class that is in package which should be scanned.
 */
public class BringApplication {
    private BringApplication() {
    }

    /**
     * Initializes and returns {@link ApplicationContext}
     * @param packagesToScan list of packages to scan
     */
    public static ApplicationContext loadContext(String... packagesToScan) {
        return getContext(packagesToScan);
    }

    /**
     * Initializes and returns {@link ApplicationContext}
     * @param clazz particular user class that is in the package which is candidate for scanning
     */
    public static ApplicationContext loadContext(Class<?> clazz) {
        return getContext(clazz.getPackageName());
    }

    /**
     * Initializes input parameters (list of {@link BeanScanner}, {@link BeanDefinitionValidator}, {@link BeanInitializer})
     * for ApplicationContext creation and return instance of context
     *
     * @param packagesToScan list of packages for scanning to define bean configs
     * @return instance of {@link ApplicationContextImpl}
     */
    private static ApplicationContext getContext(String... packagesToScan)  {
        if (packagesToScan == null || packagesToScan.length == 0) {
            throw new IllegalArgumentException("Argument [packagesToScan] must contain at least one element");
        }
        // TODO: add initialization of scanners;
        List<BeanScanner> scanners = List.of();

        return new ApplicationContextImpl(scanners, new BeanDefinitionValidator(), new BeanInitializer());
    }


    /**
     * Creates instance of ApplicationContextBuilder class
     * @return ApplicationContextBuilder instance
     */
    static ApplicationContextBuilder getContextBuilder() {
        return new ApplicationContextBuilder();
    }

    /**
     *
     */
    static class ApplicationContextBuilder {
        private Level logLevel;
        private String[] packagesToScan;
        private Class<?> classToScan;

        public ApplicationContextBuilder logLevel(Level logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public ApplicationContextBuilder packagesToScan(String... packagesToScan) {
            this.packagesToScan = packagesToScan;
            return this;
        }

        public ApplicationContextBuilder classToScan(Class<?> classToScan){
            this.classToScan = classToScan;
            return this;
        }

        public ApplicationContext build() {
            Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            logger.setLevel(logLevel != null ? logLevel : Level.INFO);

            return getContext(addClassPackageIntoPackagesToScan());
        }

        private String[] addClassPackageIntoPackagesToScan() {
            if (classToScan != null) {
                if (packagesToScan != null && packagesToScan.length != 0) {
                    List<String> packages = new ArrayList<>(List.of(this.packagesToScan));
                    packages.add(classToScan.getPackageName());
                    return packages.toArray(new String[packages.size()]);
                }
                return new String[]{classToScan.getPackageName()};
            }
            return packagesToScan;
        }


    }

}
