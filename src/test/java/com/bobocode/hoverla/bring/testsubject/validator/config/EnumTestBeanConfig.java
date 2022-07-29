package com.bobocode.hoverla.bring.testsubject.validator.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public enum EnumTestBeanConfig {
    ELEMENT;

    @Bean
    public String bean() {
        return "";
    }
}
