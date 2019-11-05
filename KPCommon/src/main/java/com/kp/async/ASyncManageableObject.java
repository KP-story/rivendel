package com.kp.async;

import java.util.concurrent.Future;

public interface ASyncManageableObject<T> {
    Future<T> init() throws Exception;

    Future<T> start() throws Exception;

    Future<T> stop() throws Exception;

    Future<T> process() throws Exception;
}
