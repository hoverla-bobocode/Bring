package com.bobocode.hoverla.bring.integration.beans;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBean2 {

    private final TestBean1 testBean1;

    public TestBean2(@Qualifier("testBean1") TestBean1 testBean1) {
        this.testBean1 = testBean1;
    }
}
