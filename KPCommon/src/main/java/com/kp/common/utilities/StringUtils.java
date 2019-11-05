package com.kp.common.utilities;

import com.google.common.base.CaseFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class StringUtils {

    private StringUtils() {
        // just prevent other can create new instance...
    }

    public static void main(String[] args) {
        System.out.println(getAllMatches("9c10d", "[123456789(10)jqkat][hdcs]"));
    }

    public static List<String> getAllMatches(String text, String regex) {
        List<String> matches = new ArrayList<String>();
        Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);
        while (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }


    public static final String upperCaseFirstLetter(String inputString) {
        if (inputString == null) {
            return null;
        }
        return Character.toUpperCase(inputString.charAt(0)) + inputString.substring(1);
    }


    public static String cammelToSnake(String inputString) {
        if (inputString == null || inputString.isEmpty())
            return inputString;
        return  CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, inputString);


    }




    public static String lowerCaseFirstLetter(String inputString) {
        if (inputString == null || inputString.isEmpty())
            return inputString;
        return Character.toLowerCase(inputString.charAt(0))
                + (inputString.length() == 1 ? "" : inputString.substring(1));

    }

    public static final boolean isPrinable(String str) {
        return !match(str, "\\p{C}");
    }

    public static final boolean match(String string, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(string);
        return matcher.find();
    }

    public static String implode(Object... elements) {
        if (elements != null) {
            StringBuilder sb = new StringBuilder();
            for (Object ele : elements) {
                sb.append(ele);
            }
            return sb.toString();
        }
        return null;
    }

    public static String implodeWithGlue(String glue, Object... elements) {
        if (elements != null && glue != null) {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (Object ele : elements) {
                if (!isFirst) {
                    sb.append(glue);
                } else {
                    isFirst = false;
                }
                sb.append(ele);
            }
            return sb.toString();
        }
        return null;
    }

    public static String implodeWithGlue(String glue, List<?> elements) {
        if (elements != null && glue != null) {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (Object ele : elements) {
                if (!isFirst) {
                    sb.append(glue);
                } else {
                    isFirst = false;
                }
                sb.append(ele);
            }
            return sb.toString();
        }
        return null;
    }
}
