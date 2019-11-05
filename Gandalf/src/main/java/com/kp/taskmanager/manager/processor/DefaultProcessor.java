package com.kp.taskmanager.manager.processor;

import com.kp.common.processor.Command;
import com.kp.taskmanager.network.codec.VOMessage;

/**
 * Created by kukubutukandy on 31/05/2017.
 */
@Command(command = "", isCommandDefault = true)
public class DefaultProcessor extends BaseProcessor {


    @Override
    public VOMessage _process(VOMessage message) throws Exception {
        return null;
    }
}
