package com.bobocode.hoverla.bring.integration.beans;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBean7 {

    @Qualifier("nonPrimaryThread")
    @Inject
    private Runnable action1;
    @Inject
    private Runnable action2;

}
