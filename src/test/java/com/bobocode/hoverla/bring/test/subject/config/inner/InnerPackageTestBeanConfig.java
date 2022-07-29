package com.bobocode.hoverla.bring.test.subject.config.inner;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class InnerPackageTestBeanConfig {

    @Bean
    public String innerPackageBean() {
        return "";
    }
}
