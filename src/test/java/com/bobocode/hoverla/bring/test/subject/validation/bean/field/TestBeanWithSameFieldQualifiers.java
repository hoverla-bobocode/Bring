package com.bobocode.hoverla.bring.test.subject.validation.bean.field;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBeanWithSameFieldQualifiers {

    @Inject
    @Qualifier("string")
    private String firstString;

    @Inject
    @Qualifier("string")
    private String secondString;

}
