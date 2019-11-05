package com.kp.thread;


public interface LifeCycle {
    int ACTIVE = 1;
    int DEACTIVE = 2;
    int PAUSED = 3;

    public int getStatus();

    public String getName();

    public void setName(String name);


    public void onCreate(Object message) throws Exception;

    public void onResume(Object message) throws Exception;

    public void onStart(Object message) throws Exception;

    public void onStop(Object message) throws Exception;

    public void onDestroy(Object message) throws Exception;

    public void onReStart(Object message) throws Exception;

}
