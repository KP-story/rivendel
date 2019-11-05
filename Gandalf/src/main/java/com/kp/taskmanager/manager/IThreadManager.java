package com.kp.taskmanager.manager;

import com.kp.common.log.Loggable;
import com.kp.taskmanager.thread.ManageableThread;
import com.kp.thread.ManageableObject;

import java.util.Collection;

public interface IThreadManager extends Loggable, ManageableObject {
    void logToFile(Object log);

    void logToMonitor(Object log);

    void logToFile(String region, Object log);

    String genTransId(String threadId);

    Collection<ManageableThread> listThreads();

    ManageableThread getThreadById(String id);


}
