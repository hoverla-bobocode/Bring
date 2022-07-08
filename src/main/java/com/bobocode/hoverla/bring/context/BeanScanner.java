package com.bobocode.hoverla.bring.context;

import java.util.List;

/**
 * Encapsulates bean scanning logic from arbitrary resources.
 */
public interface BeanScanner {
    List<BeanDefinition> scan();
}
