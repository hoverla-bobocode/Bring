package com.bobocode.hoverla.bring.test.subject.bean.util;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBean5 {

    @Inject
    @Qualifier("bean4")
    private TestBean4 testBean4;

    private final TestBean1 testBean1;

    public TestBean5(@Qualifier("bean1") TestBean1 testBean1) {
        this.testBean1 = testBean1;
    }

}
