package com.bobocode.hoverla.bring.test.subject.config;

import com.bobocode.hoverla.bring.annotation.Bean;

public class TestBeanConfigWithoutAnnotation {

    @Bean
    public String bean() {
        return "instance";
    }
}
