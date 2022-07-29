package com.bobocode.hoverla.bring.test.subject.validation.field;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean1;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean2;

@Bean
public class TestBeanWithStaticInjectFields {

    @Inject
    private static TestBean1 testBean1;

    @Inject
    private static TestBean2 testBean2;

}
