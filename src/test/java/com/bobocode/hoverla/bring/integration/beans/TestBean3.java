package com.bobocode.hoverla.bring.integration.beans;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class TestBean3 {
    @Inject
    private TestBean2 testBean2;
}
