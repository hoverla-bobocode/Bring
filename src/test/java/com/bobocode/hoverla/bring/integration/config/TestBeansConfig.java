package com.bobocode.hoverla.bring.integration.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.integration.beans.TestBean1;
import com.bobocode.hoverla.bring.integration.beans.TestBean2;

@Configuration
public class TestBeansConfig {

    @Bean
    public TestBean1 testBean1FromConfig() {
        return new TestBean1();
    }

    @Bean
    public TestBean2 testBean2FromConfig(@Qualifier("testBean1FromConfig") TestBean1 testBean1) {
        return new TestBean2(testBean1);
    }
}
