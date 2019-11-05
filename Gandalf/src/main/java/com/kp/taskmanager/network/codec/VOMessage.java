package com.kp.taskmanager.network.codec;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.vo.VObject;
import com.kp.taskmanager.manager.processor.ResultCodes;

import static com.kp.common.constant.FieldConstant.*;


public class VOMessage extends VObject implements IMessage {
    private String transId;
    protected static JsonStringMessageParser jsonStringMessageParser= new JsonStringMessageParser();
    private String command;
    private long createdTime;
    private long finishedTime;

    public VOMessage(String transId, String command) {
        put(TRANS_ID, transId);
        put(COMMAND, command);
    }

    public VOMessage(String command) {
        put(COMMAND, command);
    }

    public VOMessage() {
    }

    public VOMessage createResponse(int resultCode) {
        VOMessage response = new VOMessage();
        response.put(TRANS_ID, getId());
        response.put(COMMAND, command);
        response.put(RESULT_CODE, resultCode);

        return response;

    }

    public void setResultCode(ResultCodes resultCode) {
        put(RESULT_CODE, resultCode.getCode());
        put(RESULT_MSG, resultCode.getDesc());


    }

    public String getCommand() {
        return getString(COMMAND);
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    @Override
    public String getId() {
        return getString(TRANS_ID);
    }

    @Override
    public IMessage copy(IMessage originer) throws Exception {
        this.clear();
        this.putAll(fromJSON(jsonStringMessageParser.encodeMessage((VOMessage) originer)));
        return this;
    }



    @Override
    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long time) {
        put(CREATED_TIME, time);
    }

     public long getFinishedTime() {
        return finishedTime;
    }
}
