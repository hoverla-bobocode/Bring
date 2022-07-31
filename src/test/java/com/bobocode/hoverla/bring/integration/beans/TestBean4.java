package com.bobocode.hoverla.bring.integration.beans;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBean4 {

    @Inject
    @Qualifier("testBean2FromConfig")
    private TestBean2 testBean2;
}
