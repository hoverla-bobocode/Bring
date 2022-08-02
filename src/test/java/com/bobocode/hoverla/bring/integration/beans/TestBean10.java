package com.bobocode.hoverla.bring.integration.beans;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class TestBean10 {
    @Inject
    private TestBean8 testBean8;

    private TestBean6 testBean6;

    @Inject
    private TestBean4 testBean4;

    private TestBean9 testBean9;

    public TestBean10(TestBean6 testBean6, TestBean9 testBean9) {
        this.testBean6 = testBean6;
        this.testBean9 = testBean9;
    }
}
