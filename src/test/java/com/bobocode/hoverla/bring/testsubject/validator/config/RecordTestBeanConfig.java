package com.bobocode.hoverla.bring.testsubject.validator.config;

import com.bobocode.hoverla.bring.annotation.Bean;

public record RecordTestBeanConfig() {

    @Bean
    public String bean() {
        return "";
    }
}
