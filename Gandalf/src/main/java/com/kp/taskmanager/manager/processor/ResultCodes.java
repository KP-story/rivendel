package com.kp.taskmanager.manager.processor;

/**
 * Created by kukubutukandy on 09/05/2017.
 */
public enum ResultCodes {

    SUCCESS(0, "Success"),

    NOT_FOUND_THREAD(1, "Not found thread"),

    INCOMPLETE(2, "action incomplete please check log");;

    private final int code;
    private final String desc;

    private ResultCodes(int code, String desc) {
        this.code = code;
        this.desc = desc;

    }

    public static void main(String[] args) {
    }

    public static final String fromDesc(int code) {
        for (ResultCodes e : values()) {
            if (e.getCode() == code) {
                return e.getDesc();
            }
        }
        return null;
    }

    public static final ResultCodes fromCode(int code) {
        for (ResultCodes e : values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

    public int getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }
}
