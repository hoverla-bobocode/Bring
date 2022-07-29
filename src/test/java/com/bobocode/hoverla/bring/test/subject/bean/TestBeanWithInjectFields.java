package com.bobocode.hoverla.bring.test.subject.bean;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;
import lombok.NoArgsConstructor;

@Bean
@NoArgsConstructor
public class TestBeanWithInjectFields {

    @Inject
    @Qualifier("testBean")
    private TestBeanWithoutDependencies testBean;

    @Inject
    @Qualifier("anInteger")
    private Integer anInteger;

    @Inject
    private String aString;
}
