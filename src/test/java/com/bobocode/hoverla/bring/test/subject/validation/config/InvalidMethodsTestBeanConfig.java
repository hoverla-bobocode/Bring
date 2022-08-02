package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Configuration
public class InvalidMethodsTestBeanConfig {

    @Bean
    public static String staticMethod() {
        return EMPTY;
    }

    @Bean
    private String privateMethod() {
        return EMPTY;
    }

    @Bean
    String packagePrivateMethod() {
        return EMPTY;
    }

    @Bean
    protected String protectedMethod() {
        return EMPTY;
    }

    @Bean
    public String sameParametersTypeMethod(Integer integerOne, Integer integerTwo) {
        return EMPTY;
    }

    @Bean
    public String sameQualifiersMethod(@Qualifier("int") Integer integerOne, @Qualifier("int") Integer integerTwo) {
        return EMPTY;
    }
}
