package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ValidTestBeanConfig {

    @Bean
    public String beam1() {
        return "";
    }

    public String notBean() {
        return "";
    }

    @Bean
    public Integer bean3(int num, @Qualifier("testParamName") String name) {
        return 0;
    }

    @Bean
    public Executor bean4() {
        return Executors.newSingleThreadExecutor();
    }
}
