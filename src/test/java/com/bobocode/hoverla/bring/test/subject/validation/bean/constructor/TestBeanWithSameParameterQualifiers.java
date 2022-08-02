package com.bobocode.hoverla.bring.test.subject.validation.bean.constructor;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBeanWithSameParameterQualifiers {

    private final Integer integerOne;
    private final Integer integerTwo;

    public TestBeanWithSameParameterQualifiers(@Qualifier("int") Integer integerOne,
                                               @Qualifier("int") Integer integerTwo) {
        this.integerOne = integerOne;
        this.integerTwo = integerTwo;
    }
}
