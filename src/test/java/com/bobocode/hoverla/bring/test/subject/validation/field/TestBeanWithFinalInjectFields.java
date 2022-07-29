package com.bobocode.hoverla.bring.test.subject.validation.field;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean1;
import com.bobocode.hoverla.bring.test.subject.bean.TestBean2;
import lombok.NoArgsConstructor;

@Bean
@NoArgsConstructor(force = true)
public class TestBeanWithFinalInjectFields {

    @Inject
    private final TestBean1 testBean1;

    @Inject
    private final TestBean2 testBean2;

}
