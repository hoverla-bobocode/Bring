package com.bobocode.hoverla.bring.integration.beans;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class TestBean6 {

    private final Runnable action1;
    private final Runnable action2;

    public TestBean6(@Qualifier("nonPrimaryThread") Runnable action1, Runnable action2) {
        this.action1 = action1;
        this.action2 = action2;
    }
}
