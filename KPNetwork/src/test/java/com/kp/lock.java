package com.kp;


public class lock {
    public static int i = 1;

    public static void main(String[] args) throws InterruptedException {
//
        int[] a = {6, 3, 4, 2, 5, 6, 3, 4, 2};
        int b = a[0];
        System.out.println(6 ^ 3);
        for (int i = 1; i < a.length; i++) {
            b = b ^ a[i];
            System.out.println(Integer.toBinaryString(b) + ":" + Integer.toBinaryString(a[i]));
        }
        System.out.println(b);
    }
}
