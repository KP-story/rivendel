package com.kp.network.event;

public interface Event<T> {
    int getType();

    T getValue();

    long getTime();

}
