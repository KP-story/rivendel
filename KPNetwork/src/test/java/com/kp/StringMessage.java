package com.kp;

import com.kp.common.data.message.IMessage;

public class StringMessage implements IMessage {
    public String content;


    public StringMessage() {
    }

    public StringMessage(String content) {
        this.content = content;

    }

    @Override
    public String toString() {
        return "StringMessage{" +
                "content='" + content + '\'' +
                '}';
    }

    @Override
    public String getId() {
        String[] a = content.split(",");
        for (String b : a) {
            if (b.contains("id")) {
                String c = b.split("=")[1];
                return c.replace(";", "");
            }
        }
        return hashCode() + "";
    }

    @Override
    public IMessage copy(IMessage e) {
        return null;
    }

    @Override
    public long getCreatedTime() {
        return 0;
    }

}
