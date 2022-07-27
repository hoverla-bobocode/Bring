package com.bobocode.hoverla.bring.testsubject.scanner;

import com.bobocode.hoverla.bring.annotation.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class TestBean1 {

    private final String aString;

    private final Integer anInteger;

    private final Double aDouble;

}
