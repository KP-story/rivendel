package com.kp.common.worker;


/**
 * Created by kukubutukandy on 30/05/2017.
 */
public interface Handler<I, O> {


    O handle(I message) throws Exception;

    O interop(I requestParams) throws Exception;

}
