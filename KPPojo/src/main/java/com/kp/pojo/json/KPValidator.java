package com.kp.pojo.json;

import java.lang.annotation.Annotation;

public interface KPValidator<A extends Annotation, T> {


    default void initialize(A constraintAnnotation) {
    }

    boolean isValid(T var1);

}
