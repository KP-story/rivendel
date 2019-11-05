package com.kp.taskmanager.manager.processor;

import com.kp.common.processor.Processor;
import com.kp.taskmanager.manager.ThreadManager;
import com.kp.taskmanager.network.codec.VOMessage;

/**
 * Created by kukubutukandy on 30/05/2017.
 */
public abstract class BaseProcessor implements Processor<VOMessage, VOMessage> {

    protected ThreadManager threadManager;


    @Override
    public void init(Object... objects) throws Exception {
        this.threadManager = (ThreadManager) objects[0];

    }

    public abstract VOMessage _process(VOMessage message) throws Exception;

    @Override
    public VOMessage process(VOMessage message) throws Exception {
        getLogger().debug("message {}", message);
        return _process(message);
    }


}
