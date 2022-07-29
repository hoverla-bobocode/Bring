package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class InvalidMethodsTestBeanConfig {

    @Bean
    public static String staticMethod() {
        return "";
    }

    @Bean
    private String privateMethod() {
        return "";
    }

    @Bean
    String packagePrivateMethod() {
        return "";
    }

    @Bean
    protected String protectedMethod() {
        return "";
    }
}
