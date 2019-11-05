package com.kp.common.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    public static String toString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
