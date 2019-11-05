package com.kp.taskmanager.manager.processor;

import com.kp.common.processor.Command;
import com.kp.taskmanager.network.codec.VOMessage;
import com.kp.taskmanager.thread.ManageableThread;

import static com.kp.common.constant.FieldConstant.*;
import static com.kp.taskmanager.manager.processor.ResultCodes.*;

@Command(command = "changeStateThread")

public class ChangeStateThreadProcessor extends BaseProcessor {
    @Override
    public VOMessage _process(VOMessage message) throws Exception {

        VOMessage response = message.createResponse(SUCCESS.getCode());
        String threadId = message.getString(THREAD_ID);
        ManageableThread manageableThread = threadManager.getThreadById(threadId);
        if (manageableThread == null) {
            response.setResultCode(NOT_FOUND_THREAD);

        } else {
            try {
                int action = message.getInteger(ACTION);
                int timeout = message.getInteger(TIMEOUT);
                manageableThread.changeState(timeout, action);

            } catch (Exception e) {
                response.setResultCode(INCOMPLETE);

                getLogger().error("ChangeStateThread {} has error", manageableThread, e);
            }

        }


        return response;


    }
}
