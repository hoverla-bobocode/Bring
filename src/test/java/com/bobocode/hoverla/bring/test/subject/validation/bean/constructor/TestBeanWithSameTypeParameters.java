package com.bobocode.hoverla.bring.test.subject.validation.bean.constructor;

import com.bobocode.hoverla.bring.annotation.Bean;

@Bean
public class TestBeanWithSameTypeParameters {

    private final String stringOne;
    private final String stringTwo;

    public TestBeanWithSameTypeParameters(String stringOne, String stringTwo) {
        this.stringOne = stringOne;
        this.stringTwo = stringTwo;
    }
}
