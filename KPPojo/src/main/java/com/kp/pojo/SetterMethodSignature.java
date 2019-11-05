package com.kp.pojo;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class SetterMethodSignature extends MethodSignature {
    public String fieldName;
    public KType fieldType;


    public SetterMethodSignature(String fieldName, String methodName, Class fieldType, Type[] genericTypes, Annotation[] annotations) {
        super(methodName, annotations);

        this.fieldName = fieldName;
        if (genericTypes != null && genericTypes.length > 0) {
            Type type = genericTypes[0];
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class rawType = (Class) parameterizedType.getRawType();
                this.fieldType = new KType(rawType, parameterizedType.getActualTypeArguments());
                return;
            }

        }
        this.fieldType = new KType(fieldType);

    }

}