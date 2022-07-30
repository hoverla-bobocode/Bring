package com.bobocode.hoverla.bring.testsubject.config;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Configuration
public class TestBeanConfig {

    @Bean
    public String beanWithNoNameInAnnotation() {
        return "instance";
    }

    @Bean(name = "beanName")
    public String beanWithNameInAnnotation() {
        return "instance";
    }

    @Bean
    public Integer beanWithDependencies(int num, @Qualifier("testParamName") String name) {
        return 0;
    }

    @Bean
    public Byte beanWithDependencies(byte num, @Qualifier("testParam") String name, String anotherValue) {
        return (byte) 0;
    }

    public String notBeanMethod() {
        return "not_bean";
    }
}
