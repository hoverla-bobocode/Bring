package com.bobocode.hoverla.bring.test.subject.validation.bean.constructor;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean1;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean2;

@Bean
public class TestBeanWithInjectConstructors {

    private TestBean1 testBean1;

    private TestBean2 testBean2;

    @Inject
    public TestBeanWithInjectConstructors(TestBean1 testBean1) {
        this.testBean1 = testBean1;
    }

    @Inject
    public TestBeanWithInjectConstructors(TestBean1 testBean1, TestBean2 testBean2) {
        this.testBean1 = testBean1;
        this.testBean2 = testBean2;
    }

}
