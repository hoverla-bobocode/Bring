package com.bobocode.hoverla.bring.test.subject.validation.bean.type;

import com.bobocode.hoverla.bring.annotation.Bean;

public class OuterTestBean {

    @Bean
    public class InnerTestBean {

    }

    @Bean
    public static class NestedTestBean {

    }
}
