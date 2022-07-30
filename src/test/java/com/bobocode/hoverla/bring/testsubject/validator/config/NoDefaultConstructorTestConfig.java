package com.bobocode.hoverla.bring.testsubject.validator.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class NoDefaultConstructorTestConfig {
    public NoDefaultConstructorTestConfig(String[] args) {

    }

    private NoDefaultConstructorTestConfig() {

    }

    @Bean
    public String bean() {
        return "";
    }
}
