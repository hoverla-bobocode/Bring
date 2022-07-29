package com.bobocode.hoverla.bring.test.subject.bean.util;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class TestBean3 {

    private final TestBean1 testBean1;

    @Inject
    private TestBean2 testBean2;

    @Inject
    public TestBean3(TestBean1 testBean1) {
        this.testBean1 = testBean1;
    }


}
