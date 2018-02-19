package com.yuneec.curve;

public class JniTools {
    private static int mObject = 0;

    public static native void destoryFromJNI();

    public static native boolean getCurvePointFromJNI(int[] iArr, int[] iArr2, int i, int i2, int i3, int i4, int[] iArr3);

    public static native int getValueYFromJNI(int i);

    static {
        System.loadLibrary("CurveFunction");
    }
}
