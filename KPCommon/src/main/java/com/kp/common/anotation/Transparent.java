package com.kp.common.anotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target({FIELD, METHOD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transparent {

}
