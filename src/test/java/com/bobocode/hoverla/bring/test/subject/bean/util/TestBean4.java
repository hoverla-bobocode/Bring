package com.bobocode.hoverla.bring.test.subject.bean.util;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class TestBean4 {

    @Inject
    private TestBean2 testBean2;

    @Inject
    private TestBean3 testBean3;

}
