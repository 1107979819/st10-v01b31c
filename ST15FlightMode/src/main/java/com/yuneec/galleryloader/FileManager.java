package com.yuneec.galleryloader;

import android.os.Environment;

public class FileManager {
    public static String getCacheFilePath() {
        if (hasSDCard()) {
            return getRootFilePath() + "c-go1/caches/";
        }
        return getRootFilePath() + "c-go1/caches/";
    }

    public static String getSaveImagePath() {
        return new StringBuilder(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath())).append("/c-go1/").toString();
    }

    public static String getSaveVideoPath() {
        return new StringBuilder(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath())).append("/c-go1/").toString();
    }

    public static boolean hasSDCard() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return true;
        }
        return false;
    }

    public static String getRootFilePath() {
        return "/sdcard/";
    }
}
