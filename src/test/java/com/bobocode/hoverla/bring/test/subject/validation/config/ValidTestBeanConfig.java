package com.bobocode.hoverla.bring.test.subject.validation.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Configuration
public class ValidTestBeanConfig {

    @Bean
    public String beam1() {
        return EMPTY;
    }

    public String notBean() {
        return EMPTY;
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
