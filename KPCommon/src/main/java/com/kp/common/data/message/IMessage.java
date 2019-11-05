package com.kp.common.data.message;

/**
 * Created by khanhlv on 11/03/2019.
 */
public interface IMessage  {
    String getId();
    IMessage  copy(IMessage e) throws Exception;
    long getCreatedTime();
}
