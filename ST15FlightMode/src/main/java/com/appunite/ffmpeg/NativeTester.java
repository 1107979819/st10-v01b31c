package com.appunite.ffmpeg;

class NativeTester {
    native boolean isNeon();

    NativeTester() {
    }

    static {
        System.loadLibrary("nativetester-jni");
    }
}
