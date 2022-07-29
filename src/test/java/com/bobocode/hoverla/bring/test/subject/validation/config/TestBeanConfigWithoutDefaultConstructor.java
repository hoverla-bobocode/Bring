package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class TestBeanConfigWithoutDefaultConstructor {

    public TestBeanConfigWithoutDefaultConstructor(String[] args) {

    }

    private TestBeanConfigWithoutDefaultConstructor() {

    }

    @Bean
    public String bean() {
        return "";
    }
}
