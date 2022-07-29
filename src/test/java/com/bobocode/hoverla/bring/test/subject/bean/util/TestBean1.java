package com.bobocode.hoverla.bring.test.subject.bean.util;

import com.bobocode.hoverla.bring.annotation.Bean;
import lombok.RequiredArgsConstructor;

@Bean(name = "testBean1")
@RequiredArgsConstructor
public class TestBean1 {

    private final String aString;

    private final Integer anInteger;

    private final Double aDouble;

}
