package com.kp.pojo;

import java.lang.annotation.Annotation;

public class MethodSignature {
    public String methodName;
    public Annotation[] annotations;

    public MethodSignature(String methodName, Annotation[] annotations) {
        this.methodName = methodName;
        this.annotations = annotations;
    }
}
