package com.bobocode.hoverla.bring.testsubject;

import com.bobocode.hoverla.bring.annotation.Bean;

public class NotMarkedTestConfig {
    @Bean
    public String bean() {
        return "instance";
    }
}
