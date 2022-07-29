package com.bobocode.hoverla.bring.testsubject.scanner;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class TestBeanConfig {

    @Bean
    public String bean() {
        return "";
    }

    public String notBean() {
        return "";
    }

    @Bean
    public Integer bean2(int num, @Qualifier("testParamName") String name) {
        return 0;
    }

    @Bean
    public Executor bean3() {
        return Executors.newSingleThreadExecutor();
    }
}
