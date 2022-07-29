package com.bobocode.hoverla.bring.test.subject.bean;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TestBeanWithInjectFieldsAndConstructor {

    private final String aString;

    @Inject
    @Qualifier("int")
    private Integer anInteger;

    @Inject
    private Double aDouble;

}
