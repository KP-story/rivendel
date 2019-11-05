package com.kp.pojo.json;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface KPJsonValid {
    String message() default "Invalid data format";

    Class<? extends KPValidator<?, ?>>[] validatedBy();
}
