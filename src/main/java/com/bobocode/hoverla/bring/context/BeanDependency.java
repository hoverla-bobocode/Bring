package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Qualifier;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = {"name"})
public class BeanDependency {

    @Setter
    private String name;

    private Class<?> type;

    @Nullable
    private Class<?> genericType;

    private boolean qualified;

    public static BeanDependency fromParameter(Parameter parameter) {
        String name = parameter.getType().getName();
        boolean qualified = false;

        if (parameter.isAnnotationPresent(Qualifier.class)) {
            name = parameter.getAnnotation(Qualifier.class).value();
            qualified = true;
        }
        Class<?> type = parameter.getType();
        return new BeanDependency(name, type, null, qualified);
    }

    public static BeanDependency fromField(Field field) {
        String name = field.getType().getName();
        boolean qualified = false;

        if (field.isAnnotationPresent(Qualifier.class)) {
            name = field.getAnnotation(Qualifier.class).value();
            qualified = true;
        }
        Class<?> fieldType = field.getType();

        return new BeanDependency(name, fieldType, null, qualified);
    }
}
