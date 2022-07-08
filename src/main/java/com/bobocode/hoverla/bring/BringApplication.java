package com.bobocode.hoverla.bring;

import com.bobocode.hoverla.bring.context.ApplicationContext;

/**
 * <pre>
 * Bring starting point. Initializes and returns {@link ApplicationContext} encapsulating
 * all necessary logic for its creation.
 *
 * Usage:
 * {@code
 *     ApplicationContext = BringApplication.loadContext();
 *     String bean = ApplicationContext.getBean("bean_name", String.class);
 * }
 * </pre>
 */
public class BringApplication {
    /**
     * Initializes and returns {@link ApplicationContext}
     */
    public static ApplicationContext loadContext() {
        return null;
    }

    private BringApplication() {}
}
