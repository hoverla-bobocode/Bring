package com.bobocode.hoverla.bring.integration.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.integration.beans.TestBean1;
import com.bobocode.hoverla.bring.integration.beans.TestBean2;
import com.bobocode.hoverla.bring.integration.beans.TestBean5;
import com.bobocode.hoverla.bring.integration.beans.TestBean6;
import com.bobocode.hoverla.bring.integration.beans.TestBean8;
import com.bobocode.hoverla.bring.integration.beans.TestBean9;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TestBeansConfig {

    @Bean(primary = true)
    public TestBean1 testBean1FromConfig() {
        return new TestBean1();
    }

    @Bean
    public TestBean2 testBean2FromConfig(@Qualifier("testBean1FromConfig") TestBean1 testBean1) {
        return new TestBean2(testBean1);
    }

    @Bean(primary = true)
    public Thread primaryThread() {
        return new Thread();
    }

    @Bean
    public Thread nonPrimaryThread() {
        return new Thread();
    }

    @Bean
    public Thread nonPrimaryThread2() {
        return new Thread();
    }

    @Bean
    public TestBean5 takesInterface(Runnable runnable) {
        return new TestBean5(runnable);
    }

    @Bean
    public Object takesQualifiedThread(@Qualifier("nonPrimaryThread") Runnable action1, Runnable action2) {
        return new TestBean6(action1, action2);
    }

    @Bean
    public Object objectBean() {
        return new Object();
    }

    @Bean
    public TestBean8 testBean8FromConfig(@Qualifier("testBean1FromConfig") TestBean1 testBean1) {
        return new TestBean8(testBean1);
    }

    @Bean
    public TestBean9 testBean9FromConfig() {
        return new TestBean9();
    }
}
