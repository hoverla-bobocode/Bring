package com.bobocode.hoverla.bring.test.subject.bean;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TestBeanWithInjectConstructor {

    private final TestBeanWithoutDependencies testBeanWithoutDependencies;

}
