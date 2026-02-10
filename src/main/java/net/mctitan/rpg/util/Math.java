package net.mctitan.rpg.util;

import java.util.Random;

public class Math {
    private static final Random RANDOM = new Random();

    public static final double PI = java.lang.Math.PI;

    public static Random random() { return RANDOM; }

    public static int nextInt() { return RANDOM.nextInt(); }
    public static int nextInt(int max) {
        return RANDOM.nextInt(max);
    }
    public static int nextInt(int min, int max) { return min + nextInt(max - min + 1); }

    public static long nextLong() { return RANDOM.nextLong(); }

    public static double nextDouble() { return RANDOM.nextDouble(); }
    public static double nextDouble(double min, double max) { return min + (max - min) * RANDOM.nextDouble(); }

    public static double pow(double base, double exp) { return java.lang.Math.pow(base, exp); }

    public static int abs(int value) { return java.lang.Math.abs(value); }
    public static double abs(double value) { return java.lang.Math.abs(value); }

    public static double round(double value, double precision) {
        double ret;
        if(precision < 0) { throw new IllegalArgumentException("precision < 0"); }
        else if(precision == 0) {
            ret = value;
        } else {
            ret = java.lang.Math.floor(java.lang.Math.round(65536f * value / precision) / 65536f) * precision;
        }

        return ret;
    }

    public static int max(int a, int b) { return (java.lang.Math.max(a, b)); }
    public static double max(double a, double b) { return (java.lang.Math.max(a, b)); }

    public static int min(int a, int b) { return (java.lang.Math.min(a, b)); }
    public static double min(double a, double b) { return (java.lang.Math.min(a, b)); }

    public static double sin(double a) { return java.lang.Math.sin(a); }
    public static double cos(double a) { return java.lang.Math.cos(a); }
    public static double tan(double a) { return java.lang.Math.tan(a); }
}
