package com.kp.thread;

public interface ManageableObject {
    void init() throws Exception;

    void start() throws Exception;

    void stop() throws Exception;

    void process() throws Exception;
}
