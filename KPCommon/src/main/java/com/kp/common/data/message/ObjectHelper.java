package com.kp.common.data.message;

public interface ObjectHelper<T> {
    T newInstance(Object... params);

}