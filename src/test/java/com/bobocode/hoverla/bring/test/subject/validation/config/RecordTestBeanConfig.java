package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;

public record RecordTestBeanConfig() {

    @Bean
    public String bean() {
        return "";
    }
}
