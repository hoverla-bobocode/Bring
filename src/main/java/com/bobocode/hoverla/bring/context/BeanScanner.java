package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Bean;

import java.util.List;

/**
 * Encapsulates bean scanning logic from arbitrary resources.
 *
 * <p>Specific functionality is supplied by concrete implementations.</p>
 *
 * @see Bean @Bean
 * @see BeanDefinition
 * @see BeanAnnotationScanner
 */
public interface BeanScanner {

    List<BeanDefinition> scan();

}
