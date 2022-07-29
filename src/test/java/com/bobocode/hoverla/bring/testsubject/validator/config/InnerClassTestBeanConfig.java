package com.bobocode.hoverla.bring.testsubject.validator.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

public class InnerClassTestBeanConfig {

    @Configuration
    public class InnerTestBeanConfig {
        @Bean
        public String bean() {
            return "";
        }
    }
}
