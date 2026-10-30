package com.iafenvoy.jupiter.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.function.Predicate;

public class JupiterUtils {
    public static Class<?> getGenericActualClass(Field field) {
        return field.getGenericType() instanceof ParameterizedType parameterized && parameterized.getActualTypeArguments().length > 0 && parameterized.getActualTypeArguments()[0] instanceof Class<?> actualClazz ? actualClazz : null;
    }

    public static <T> Predicate<T> packPredicate(final Predicate<T> parent) {
        return v -> {
            try {
                return parent.test(v);
            } catch (Exception e) {
                return false;
            }
        };
    }
}
