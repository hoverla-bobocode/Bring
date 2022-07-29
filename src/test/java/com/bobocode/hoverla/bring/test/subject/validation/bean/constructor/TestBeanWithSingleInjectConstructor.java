package com.bobocode.hoverla.bring.test.subject.validation.bean.constructor;

import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Bean
@NoArgsConstructor(access = AccessLevel.PUBLIC, onConstructor = @__(@Inject))
public class TestBeanWithSingleInjectConstructor {
}
