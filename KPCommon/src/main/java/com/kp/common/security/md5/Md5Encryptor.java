package com.kp.common.security.md5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Md5Encryptor {

    public static final byte[] encrypt(byte[] data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(data);
        return md.digest();
    }

    public static void main(String... argu) {
        String data = "ABCDEF12345678901234";
        String sign = "e0a64e564380c76660c2ccfec1bfd6a1";
        System.out.print(encryptToHex(data.getBytes()));
        System.out.print("" + validateHex(sign, data));
    }

    public static final byte[] encrypt(byte[]... data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        for (byte[] bytes : data) {
            md.update(bytes);
        }
        return md.digest();
    }

    public static final String encryptToHex(byte[] data) {
        byte[] hash = encrypt(data);
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        return hexString.toString();
    }


    public final static boolean validate(byte[] validSignature, byte[] data) {
        return Arrays.equals(validSignature, encrypt(data));
    }

    public final static boolean validateHex(String validSignature, String data) {
        return validSignature.equals(encryptToHex(data.getBytes()));
    }

}
