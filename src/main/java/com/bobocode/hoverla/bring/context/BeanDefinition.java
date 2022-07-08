package com.bobocode.hoverla.bring.context;

import java.util.List;

/**
 * Wraps all necessary bean information, encapsulates bean creation logic (this logic can be seperated in future)
 */
public interface BeanDefinition {
    String name();
    Class<?> type();
    List<String> dependenciesNames();
    Object instance(BeanDefinition... dependencies);
}
