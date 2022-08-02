package com.bobocode.hoverla.bring.test.subject.validation.bean.field;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class TestBeanWithSameTypeInjectFields {

    @Inject
    private Integer firstInteger;

    @Inject
    private Integer secondInteger;

}
