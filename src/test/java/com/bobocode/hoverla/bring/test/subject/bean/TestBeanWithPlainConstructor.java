package com.bobocode.hoverla.bring.test.subject.bean;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBeanWithPlainConstructor {

    private final TestBeanWithoutDependencies testBean1;

    private final TestBeanWithoutDependencies testBean2;

    public TestBeanWithPlainConstructor(@Qualifier("testBean") TestBeanWithoutDependencies testBean1,
                                        TestBeanWithoutDependencies testBean2) {
        this.testBean1 = testBean1;
        this.testBean2 = testBean2;
    }

}
