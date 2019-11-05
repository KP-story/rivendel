package com.kp.common.processor;


import com.kp.common.log.Loggable;

/**
 * Created by kukubutukandy on 30/05/2017.
 */
public interface Processor<I, O> extends Loggable {

    void init(Object... value) throws Exception;

    O process(I message) throws Exception;


}
