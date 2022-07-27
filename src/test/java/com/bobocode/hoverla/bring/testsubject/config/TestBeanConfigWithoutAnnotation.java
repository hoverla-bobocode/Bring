package com.bobocode.hoverla.bring.testsubject.config;

import com.bobocode.hoverla.bring.annotation.Bean;

public class TestBeanConfigWithoutAnnotation {

    @Bean
    public String bean() {
        return "instance";
    }
}
