package com.bobocode.hoverla.bring.test.subject.validation.constructor;

import com.bobocode.hoverla.bring.annotation.Bean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Bean
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestBeanWithoutConstructors {

}
