package com.bobocode.hoverla.bring.test.subject.bean.util;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TestBean2 {

    private final TestBean1 testBean1;

}
