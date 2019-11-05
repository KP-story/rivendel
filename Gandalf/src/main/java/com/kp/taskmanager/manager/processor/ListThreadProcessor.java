package com.kp.taskmanager.manager.processor;

import com.kp.common.data.vo.VArray;
import com.kp.common.processor.Command;
import com.kp.taskmanager.network.codec.VOMessage;
import com.kp.taskmanager.thread.ManageableThread;

import java.util.Collection;

import static com.kp.common.constant.FieldConstant.*;
import static com.kp.taskmanager.manager.processor.ResultCodes.SUCCESS;

@Command(command = "listThread")
public class ListThreadProcessor extends BaseProcessor {
    @Override
    public VOMessage _process(VOMessage message) throws Exception {
        Collection<ManageableThread> manageableThreads = threadManager.listThreads();
        VOMessage response = message.createResponse(SUCCESS.getCode());
        VArray threads = new VArray();
        for (ManageableThread manageableThread : manageableThreads) {
            VOMessage threadInfo = new VOMessage();
            threadInfo.put(THREAD_NAME, manageableThread.getThreadName());
            threadInfo.put(THREAD_ID, manageableThread.getThreadId());
            threadInfo.put(STATE, manageableThread.getStatus());
            threads.add(threadInfo);
        }

        response.put(THREADS, threads);

        return response;
    }
}
