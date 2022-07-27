package com.bobocode.hoverla.bring.testsubject.validator.type;

import com.bobocode.hoverla.bring.annotation.Bean;

public class OuterTestBean {

    @Bean
    public class InnerTestBean {

    }

    @Bean
    public static class NestedTestBean {

    }
}
