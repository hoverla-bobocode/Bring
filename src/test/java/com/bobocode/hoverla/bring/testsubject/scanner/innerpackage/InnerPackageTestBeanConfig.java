package com.bobocode.hoverla.bring.testsubject.scanner.innerpackage;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class InnerPackageTestBeanConfig {
    @Bean
    public String innerPackageBean() {
        return "";
    }
}
