package com.kp.common.utilities;

public class DataTypeConverter {


    public static String byteArrayToHexString(byte[] in, boolean columnize) {
        if (in == null || in.length <= 0) {
            return "";
        }
        String pseudo = "0123456789ABCDEF";

        StringBuffer out = new StringBuffer(in.length * 3);

        for (int i = 0; i < in.length; i++) {
            byte ch = in[i];
            out.append(pseudo.charAt((int) ((ch & 0xF0) >> 4)));
            out.append(pseudo.charAt((int) (ch & 0x0F)));

            if (columnize) {
                if ((i + 1) % 16 == 0) {
                    out.append("\n");
                } else if ((i + 1) % 4 == 0) {
                    out.append(" ");
                }
            }
        }

        return out.toString();
    }

    public static String byteArrayToHexStringLine(byte[] in) {
        return byteArrayToHexString(in, false);
    }

    public static String byteArrayToHexString(byte[] in) {
        return byteArrayToHexString(in, true);
    }


    public static byte[] longToBytes(long value) {
        byte[] data = new byte[4];
        data[0] = (byte) ((value >> 24) & 0xFF);
        data[1] = (byte) ((value >> 16) & 0xFF);
        data[2] = (byte) ((value >> 8) & 0xFF);
        data[3] = (byte) ((value) & 0xFF);
        return data;
    }


    public static short bytesToShort(byte hi, byte lo) {
        return (short) (lo << 8 | hi & 255);
    }

    public static byte[] shortToBytes(short number) {
        return new byte[]{(byte) (number >> 8), (byte) number};

    }


    public static long byteArrayToLong(byte[] bytes, int index) {
        return fromBytes(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
    }

    public static long fromBytes(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        return ((long) b1 & 255L) << 56 | ((long) b2 & 255L) << 48 | ((long) b3 & 255L) << 40 | ((long) b4 & 255L) << 32 | ((long) b5 & 255L) << 24 | ((long) b6 & 255L) << 16 | ((long) b7 & 255L) << 8 | (long) b8 & 255L;
    }


    public static byte[] longToByteArray(long value) {
        return new byte[]{(byte) ((int) (value >> 56)), (byte) ((int) (value >> 48)), (byte) ((int) (value >> 40)), (byte) ((int) (value >> 32)), (byte) ((int) (value >> 24)), (byte) ((int) (value >> 16)), (byte) ((int) (value >> 8)), (byte) ((int) value)};
    }

    public static void main(String[] args) {


        long data = Long.valueOf("9223372036854775807");


        byte[] bytes = longToByteArray(data);
        long data2 = byteArrayToLong(bytes, 0);
        System.out.printf(data2 + "");
    }


    public static int byteArrayToInt(byte[] b, int index) {

        return b[index] << 24 | (b[index + 1] & 255) << 16 | (b[index + 2] & 255) << 8 | b[index + 3] & 255;


    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};

    }


}
