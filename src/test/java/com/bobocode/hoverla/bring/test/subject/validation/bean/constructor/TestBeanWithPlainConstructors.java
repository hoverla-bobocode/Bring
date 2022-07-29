package com.bobocode.hoverla.bring.test.subject.validation.bean.constructor;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.test.subject.bean.util.TestBean1;

@Bean
public class TestBeanWithPlainConstructors {

    private TestBean1 testBean1;

    private String aString;

    public TestBeanWithPlainConstructors() {

    }

    public TestBeanWithPlainConstructors(TestBean1 testBean1) {
        this.testBean1 = testBean1;
    }

    public TestBeanWithPlainConstructors(TestBean1 testBean1, String aString) {
        this.testBean1 = testBean1;
        this.aString = aString;
    }

}
