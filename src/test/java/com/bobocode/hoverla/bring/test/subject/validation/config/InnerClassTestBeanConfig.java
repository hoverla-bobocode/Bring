package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class InnerClassTestBeanConfig {

    @Configuration
    public class InnerTestBeanConfig {
        @Bean
        public String bean() {
            return EMPTY;
        }
    }
}
