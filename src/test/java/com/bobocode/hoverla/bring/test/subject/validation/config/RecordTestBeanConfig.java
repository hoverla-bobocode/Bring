package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public record RecordTestBeanConfig() {

    @Bean
    public String bean() {
        return EMPTY;
    }
}
