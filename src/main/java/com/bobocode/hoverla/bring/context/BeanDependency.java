package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.annotation.Qualifier;
import com.bobocode.hoverla.bring.exception.BeanDependencyInjectionException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = {"name"})
public class BeanDependency {

    @Setter
    private String name;

    private Class<?> type;

    private boolean qualified;

    private boolean collection;

    @Nullable
    private Class<?> collectionGenericType;

    public BeanDependency(String name, Class<?> type, boolean qualified) {
        this.name = name;
        this.type = type;
        this.qualified = qualified;
    }

    public static BeanDependency fromParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();

        String dependencyName = parameterType.getName();
        boolean qualified = false;
        if (parameter.isAnnotationPresent(Qualifier.class)) {
            dependencyName = parameter.getAnnotation(Qualifier.class).value();
            qualified = true;
        }

        Class<?> collectionGenericType = null;
        boolean isCollection = false;
        if (Collection.class.isAssignableFrom(parameterType)) { // only generic collections supported for now
            isCollection = true;

            try {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                collectionGenericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                if (!qualified) {
                    dependencyName = String.join("#", dependencyName, collectionGenericType.getName());
                }
            } catch (ClassCastException ex) {
                throw new BeanDependencyInjectionException("Parameter %s is a Collection of raw type"
                        .formatted(parameter.getName()));
            }
        }

        return new BeanDependency(dependencyName, parameterType, qualified, isCollection, collectionGenericType);
    }

    public static BeanDependency fromField(Field field) {
        Class<?> fieldType = field.getType();

        String dependencyName = fieldType.getName();
        boolean qualified = false;
        if (field.isAnnotationPresent(Qualifier.class)) {
            dependencyName = field.getAnnotation(Qualifier.class).value();
            qualified = true;
        }

        Class<?> collectionGenericType = null;
        boolean isCollection = false;
        if (Collection.class.isAssignableFrom(fieldType)) { // only generic collections supported for now
            isCollection = true;

            try {
                ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
                collectionGenericType = ((Class<?>) fieldGenericType.getActualTypeArguments()[0]);
                if (!qualified) {
                    dependencyName = String.join("#", dependencyName, collectionGenericType.getName());
                }
            } catch (ClassCastException ex) {
                throw new BeanDependencyInjectionException("Field %s is a Collection of raw type"
                        .formatted(field.getName()));
            }
        }

        return new BeanDependency(dependencyName, fieldType, qualified, isCollection, collectionGenericType);
    }
}
